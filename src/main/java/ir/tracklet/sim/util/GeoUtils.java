package ir.tracklet.sim.util;

public final class GeoUtils {
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private GeoUtils() {}

    public static double moveLatitude(double lat, double distanceKm, double bearingDegrees) {
        double angular = distanceKm / EARTH_RADIUS_KM;
        double bearing = Math.toRadians(bearingDegrees);
        double current = Math.toRadians(lat);
        double next = Math.asin(
                Math.sin(current) * Math.cos(angular)
                        + Math.cos(current) * Math.sin(angular) * Math.cos(bearing));
        return Math.toDegrees(next);
    }

    public static double moveLongitude(double lat, double lon, double distanceKm, double bearingDegrees) {
        double angular = distanceKm / EARTH_RADIUS_KM;
        double bearing = Math.toRadians(bearingDegrees);
        double currentLat = Math.toRadians(lat);
        double currentLon = Math.toRadians(lon);
        double newLat = Math.asin(
                Math.sin(currentLat) * Math.cos(angular)
                        + Math.cos(currentLat) * Math.sin(angular) * Math.cos(bearing));
        double newLon = currentLon + Math.atan2(
                Math.sin(bearing) * Math.sin(angular) * Math.cos(currentLat),
                Math.cos(angular) - Math.sin(currentLat) * Math.sin(newLat));
        return normalizeLongitude(Math.toDegrees(newLon));
    }

    public static double normalizeHeading(double heading) {
        double normalized = heading % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }

    public static double normalizeLongitude(double lon) {
        double value = lon % 360.0;
        if (value > 180) value -= 360;
        if (value < -180) value += 360;
        return value;
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
