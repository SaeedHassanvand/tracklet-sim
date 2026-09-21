package ir.tracklet.sim.service;

import ir.tracklet.sim.config.SimulatorProperties;
import ir.tracklet.sim.domain.FleetType;
import ir.tracklet.sim.domain.ObjectStatus;
import ir.tracklet.sim.domain.TrajectoryPoint;
import ir.tracklet.sim.domain.TrackingObject;
import ir.tracklet.sim.repository.FleetStateRepository;
import ir.tracklet.sim.simulation.AerialMovementModel;
import ir.tracklet.sim.simulation.BrtMovementModel;
import ir.tracklet.sim.simulation.MovementModel;
import ir.tracklet.sim.simulation.MotorcycleMovementModel;
import ir.tracklet.sim.simulation.SimulatedObject;
import ir.tracklet.sim.simulation.SimulationRuntime;
import ir.tracklet.sim.util.TehranSimulationArea;
import ir.tracklet.sim.simulation.UrbanRouteCatalog;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class FleetSimulationService {
    private final SimulatorProperties properties;
    private final FleetStateRepository repository;
    private final Map<FleetType, ConcurrentHashMap<String, SimulatedObject>> objects = new EnumMap<>(FleetType.class);
    private final Map<FleetType, MovementModel> models = new EnumMap<>(FleetType.class);

    public FleetSimulationService(SimulatorProperties properties, FleetStateRepository repository) {
        this.properties = properties;
        this.repository = repository;
        initialize();
        if (properties.isEnabled()) {
            repository.clearSimulatorData();
            initializeFromScratch();
        }
    }

    private void initialize() {
        for (FleetType type : FleetType.values()) {
            objects.put(type, new ConcurrentHashMap<>());
            SimulatorProperties.FleetConfig config = properties.getFleets().get(type);
            if (config == null) continue;
            models.put(type, switch (type) {
                case AERIAL -> new AerialMovementModel(config);
                case BRT -> new BrtMovementModel(config);
                case MOTORCYCLE -> new MotorcycleMovementModel(config);
            });
        }
    }

    @Scheduled(fixedDelayString = "${simulator.engine-tick-ms:1000}")
    public void simulationTick() {
        if (!properties.isEnabled()) return;
        Instant now = Instant.now();
        for (FleetType type : FleetType.values()) {
            SimulatorProperties.FleetConfig config = properties.getFleets().get(type);
            if (config == null || !config.isEnabled()) continue;
            MovementModel model = models.get(type);
            objects.get(type).values().forEach(simulated -> updateObject(type, config, model, simulated, now));
        }
    }

    private void updateObject(FleetType type,
                              SimulatorProperties.FleetConfig config,
                              MovementModel model,
                              SimulatedObject simulated,
                              Instant now) {
        long elapsedMs = Duration.between(simulated.getLastSimulationAt(), now).toMillis();
        if (elapsedMs < config.getUpdateIntervalMs()) return;

        simulated.setLastSimulationAt(now);

        if (ThreadLocalRandom.current().nextDouble() < config.getObservationDropRate()) {
            return;
        }

        TrackingObject object = simulated.getObject();
        Duration delta = Duration.ofMillis(elapsedMs);
        model.update(object, simulated.getRuntime(), delta);
        object.setStatus(ObjectStatus.ACTIVE);
        object.setVersion(object.getVersion() + 1);

        Instant observedAt = now.minusMillis(Math.max(0, config.getNetworkDelayMs()));
        object.setLastObservedAt(observedAt);
        object.setLastReceivedAt(now);

        simulated.setLastSuccessfulObservationAt(now);
        repository.save(object,
                new TrajectoryPoint(object.getLat(), object.getLon(), object.getAlt(), observedAt),
                properties.getTrajectoryMaxPoints());
    }

    public List<TrackingObject> liveState(String fleetId, double minLon, double minLat,
                                          double maxLon, double maxLat) {
        return repository.findByBoundingBox(fleetId, minLon, minLat, maxLon, maxLat);
    }

    public TrackingObject getObject(String fleetId, String objectId) {
        return repository.find(fleetId, objectId)
                .orElseThrow(() -> new ObjectNotFoundException(fleetId, objectId));
    }

    public List<TrajectoryPoint> trajectory(String fleetId, String objectId) {
        if (!repository.trajectoryExists(fleetId, objectId)) {
            throw new ObjectNotFoundException(fleetId, objectId);
        }
        return repository.findTrajectory(fleetId, objectId);
    }

    @Scheduled(fixedDelayString = "${simulator.lifecycle-tick-ms:1000}")
    public void lifecycleTick() {
        if (!properties.isEnabled()) return;
        Instant now = Instant.now();
        Duration staleThreshold = properties.getLifecycle().getStaleThreshold();
        Duration lostThreshold = properties.getLifecycle().getLostThreshold();

        for (FleetType type : FleetType.values()) {
            for (SimulatedObject simulated : objects.get(type).values()) {
                TrackingObject object = simulated.getObject();
                Duration age = Duration.between(object.getLastReceivedAt(), now);
                if (age.compareTo(lostThreshold) > 0) {
                    object.setStatus(ObjectStatus.LOST);
                    repository.deleteLiveObject(object);
                    objects.get(type).remove(object.getId());
                } else if (age.compareTo(staleThreshold) > 0 && object.getStatus() != ObjectStatus.STALE) {
                    object.setStatus(ObjectStatus.STALE);
                    object.setVersion(object.getVersion() + 1);
                    repository.saveLiveState(object);
                }
            }
        }
    }

    public int fleetObjectCount(FleetType type) {
        return objects.get(type).size();
    }

    public void initializeFromScratch() {
        Instant now = Instant.now();
        for (FleetType type : FleetType.values()) {
            SimulatorProperties.FleetConfig config = properties.getFleets().get(type);
            if (config == null || !config.isEnabled()) continue;

            objects.get(type).clear();
            for (int i = 0; i < config.getObjectCount(); i++) {
                TrackingObject object = createObject(type, i, now, config);
                SimulationRuntime runtime = new SimulationRuntime(ThreadLocalRandom.current().nextLong());
                if (type == FleetType.BRT) {
                    runtime.setRouteIndex(i % UrbanRouteCatalog.routes().size());
                    runtime.setRoutePhase(i % UrbanRouteCatalog.routes().get(runtime.getRouteIndex()).waypoints().size());
                }
                SimulatedObject simulated = new SimulatedObject(object, runtime, now);
                objects.get(type).put(object.getId(), simulated);
                repository.save(object,
                        new TrajectoryPoint(object.getLat(), object.getLon(), object.getAlt(), object.getLastObservedAt()),
                        properties.getTrajectoryMaxPoints());
            }
        }
    }

    private TrackingObject createObject(FleetType type, int index, Instant now,
                                        SimulatorProperties.FleetConfig config) {
        TrackingObject object = new TrackingObject();
        object.setId(UUID.randomUUID().toString());
        object.setExternalId(externalId(type, index));
        object.setFleetId(type.name().toLowerCase());
        object.setFleetType(type);
        object.setStatus(ObjectStatus.ACTIVE);
        object.setVersion(1);
        object.setLastObservedAt(now);
        object.setLastReceivedAt(now);
        object.setSourceId(sourceId(type));
        object.setSource(source(type));

        double[] position = initialPosition(type, index);
        object.setLat(position[0]);
        object.setLon(position[1]);
        object.setHeading(ThreadLocalRandom.current().nextDouble(0, 360));
        object.setGroundSpeed(ThreadLocalRandom.current().nextDouble(config.getMinSpeedKmh(), config.getMaxSpeedKmh()));
        object.setVerticalSpeed(0);
        object.setAlt(type == FleetType.AERIAL
                ? ThreadLocalRandom.current().nextDouble(config.getMinAltitudeFt(), config.getMaxAltitudeFt())
                : 0);

        if (type == FleetType.AERIAL) {
            object.setIcao(String.format("%06X", ThreadLocalRandom.current().nextInt(0x100000, 0xFFFFFF)));
            object.setCallsign("IRSIM" + String.format("%04d", index + 1));
            object.setSquawk(ThreadLocalRandom.current().nextInt(1000, 7777));
        } else if (type == FleetType.BRT) {
            object.getAttributes().put("routeId", "BRT-" + (index % 3 + 1));
            object.getAttributes().put("vehicleNumber", String.format("BRT-%04d", index + 1));
        } else {
            object.getAttributes().put("courierId", String.format("MOTO-%05d", index + 1));
            object.getAttributes().put("deliveryStatus", "IN_TRANSIT");
        }
        return object;
    }

    private double[] initialPosition(FleetType type, int index) {
        if (type == FleetType.BRT) {
            var route = UrbanRouteCatalog.routes().get(index % UrbanRouteCatalog.routes().size());
            return route.waypoint(index % route.waypoints().size()).clone();
        }
        if (type == FleetType.MOTORCYCLE) {
            return TehranSimulationArea.randomPoint();
        }
        return ir.tracklet.sim.util.IranBoundary.randomPoint();
    }

    private String externalId(FleetType type, int index) {
        return switch (type) {
            case AERIAL -> "fr24-sim-" + (index + 1);
            case BRT -> "bus-sim-" + (index + 1);
            case MOTORCYCLE -> "moto-sim-" + (index + 1);
        };
    }

    private String sourceId(FleetType type) {
        return switch (type) {
            case AERIAL -> "sim-adsb";
            case BRT -> "sim-bus-gps";
            case MOTORCYCLE -> "sim-courier-gps";
        };
    }

    private String source(FleetType type) {
        return switch (type) {
            case AERIAL -> "ADSB-SIM";
            case BRT -> "BUS-GPS-SIM";
            case MOTORCYCLE -> "COURIER-GPS-SIM";
        };
    }
}
