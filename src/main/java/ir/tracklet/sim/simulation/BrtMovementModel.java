package ir.tracklet.sim.simulation;

import ir.tracklet.sim.config.SimulatorProperties.FleetConfig;
import ir.tracklet.sim.domain.TrackingObject;
import ir.tracklet.sim.util.GeoUtils;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BrtMovementModel implements MovementModel {
    private final FleetConfig config;
    private final List<TehranRoute> routes = UrbanRouteCatalog.routes();

    public BrtMovementModel(FleetConfig config) {
        this.config = config;
    }

    @Override
    public void update(TrackingObject object, SimulationRuntime runtime, Duration delta) {
        double seconds = Math.max(delta.toMillis() / 1000.0, 0.1);
        TehranRoute route = routes.get(runtime.getRouteIndex() % routes.size());
        double[] target = route.waypoint((int) runtime.getRoutePhase());

        double distanceKm = config.getMaxSpeedKmh() * seconds / 3600.0 * 0.65;
        double bearing = bearing(object.getLat(), object.getLon(), target[0], target[1]);
        double jitter = ThreadLocalRandom.current().nextDouble(-4.0, 4.0);
        bearing = GeoUtils.normalizeHeading(bearing + jitter);

        double nextLat = GeoUtils.moveLatitude(object.getLat(), distanceKm, bearing);
        double nextLon = GeoUtils.moveLongitude(object.getLat(), object.getLon(), distanceKm, bearing);
        object.setLat(nextLat);
        object.setLon(nextLon);
        object.setHeading(bearing);
        object.setGroundSpeed(ThreadLocalRandom.current().nextDouble(config.getMinSpeedKmh(), config.getMaxSpeedKmh()));
        object.setAlt(0);
        object.setVerticalSpeed(0);

        double targetDistance = planarDistance(object.getLat(), object.getLon(), target[0], target[1]);
        if (targetDistance < 0.003) {
            runtime.setRoutePhase(runtime.getRoutePhase() + 1);
            if ((int) runtime.getRoutePhase() >= route.waypoints().size()) {
                runtime.setRouteIndex((runtime.getRouteIndex() + 1) % routes.size());
                runtime.setRoutePhase(0);
            }
        }
    }

    private double bearing(double lat1, double lon1, double lat2, double lon2) {
        double y = Math.sin(Math.toRadians(lon2 - lon1)) * Math.cos(Math.toRadians(lat2));
        double x = Math.cos(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                - Math.sin(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.cos(Math.toRadians(lon2 - lon1));
        return GeoUtils.normalizeHeading(Math.toDegrees(Math.atan2(y, x)));
    }

    private double planarDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = (lat2 - lat1) * 111.0;
        double dLon = (lon2 - lon1) * 111.0 * Math.cos(Math.toRadians((lat1 + lat2) / 2.0));
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }
}
