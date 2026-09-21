package ir.tracklet.sim.simulation;

import ir.tracklet.sim.config.SimulatorProperties.FleetConfig;
import ir.tracklet.sim.domain.TrackingObject;
import ir.tracklet.sim.util.GeoUtils;
import ir.tracklet.sim.util.TehranSimulationArea;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class MotorcycleMovementModel implements MovementModel {
    private final FleetConfig config;

    public MotorcycleMovementModel(FleetConfig config) {
        this.config = config;
    }

    @Override
    public void update(TrackingObject object, SimulationRuntime runtime, Duration delta) {
        double seconds = Math.max(delta.toMillis() / 1000.0, 0.1);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double turn = random.nextDouble(-18.0, 18.0);
        double heading = GeoUtils.normalizeHeading(object.getHeading() + turn);
        double speed = GeoUtils.clamp(
                object.getGroundSpeed() + random.nextDouble(-5, 5),
                config.getMinSpeedKmh(), config.getMaxSpeedKmh());
        double distanceKm = speed * seconds / 3600.0;

        double nextLat = GeoUtils.moveLatitude(object.getLat(), distanceKm, heading);
        double nextLon = GeoUtils.moveLongitude(object.getLat(), object.getLon(), distanceKm, heading);
        if (!TehranSimulationArea.contains(nextLat, nextLon)) {
            heading = GeoUtils.normalizeHeading(heading + 180.0 + random.nextDouble(-30, 30));
            nextLat = GeoUtils.moveLatitude(object.getLat(), distanceKm, heading);
            nextLon = GeoUtils.moveLongitude(object.getLat(), object.getLon(), distanceKm, heading);
        }

        object.setHeading(heading);
        object.setGroundSpeed(speed);
        object.setLat(GeoUtils.clamp(nextLat, TehranSimulationArea.MIN_LAT, TehranSimulationArea.MAX_LAT));
        object.setLon(GeoUtils.clamp(nextLon, TehranSimulationArea.MIN_LON, TehranSimulationArea.MAX_LON));
        object.setAlt(0);
        object.setVerticalSpeed(0);
    }
}
