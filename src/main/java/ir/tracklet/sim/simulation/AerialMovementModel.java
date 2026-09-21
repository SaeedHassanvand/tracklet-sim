package ir.tracklet.sim.simulation;

import ir.tracklet.sim.config.SimulatorProperties.FleetConfig;
import ir.tracklet.sim.domain.TrackingObject;
import ir.tracklet.sim.util.GeoUtils;
import ir.tracklet.sim.util.IranBoundary;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public class AerialMovementModel implements MovementModel {
    private final FleetConfig config;

    public AerialMovementModel(FleetConfig config) {
        this.config = config;
    }

    @Override
    public void update(TrackingObject object, SimulationRuntime runtime, Duration delta) {
        double seconds = Math.max(delta.toMillis() / 1000.0, 0.1);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double headingChange = random.nextDouble(-3.0, 3.0);
        double heading = GeoUtils.normalizeHeading(object.getHeading() + headingChange);
        double speedKmh = GeoUtils.clamp(
                object.getGroundSpeed() + random.nextDouble(-12.0, 12.0),
                config.getMinSpeedKmh(), config.getMaxSpeedKmh());
        double distanceKm = speedKmh * seconds / 3600.0;

        double nextLat = GeoUtils.moveLatitude(object.getLat(), distanceKm, heading);
        double nextLon = GeoUtils.moveLongitude(object.getLat(), object.getLon(), distanceKm, heading);
        if (!IranBoundary.contains(nextLon, nextLat)) {
            heading = GeoUtils.normalizeHeading(heading + 170.0 + random.nextDouble(-20, 20));
            nextLat = GeoUtils.moveLatitude(object.getLat(), distanceKm, heading);
            nextLon = GeoUtils.moveLongitude(object.getLat(), object.getLon(), distanceKm, heading);
        }

        object.setHeading(heading);
        object.setGroundSpeed(speedKmh);
        object.setLat(nextLat);
        object.setLon(nextLon);

        double altitude = GeoUtils.clamp(
                object.getAlt() + random.nextDouble(-250.0, 250.0),
                config.getMinAltitudeFt(), config.getMaxAltitudeFt());
        object.setAlt(altitude);
        object.setVerticalSpeed(random.nextDouble(-800.0, 800.0));
    }
}
