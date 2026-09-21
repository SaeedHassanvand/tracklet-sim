package ir.tracklet.sim.repository;

public final class RedisKeys {
    private static final String PREFIX = "tracklet-sim:";

    private RedisKeys() {}

    public static String object(String fleetId, String objectId) {
        return PREFIX + "fleet:" + fleetId + ":object:" + objectId;
    }

    public static String geo(String fleetId) {
        return PREFIX + "fleet:" + fleetId + ":geo";
    }

    public static String trajectory(String fleetId, String objectId) {
        return PREFIX + "fleet:" + fleetId + ":trajectory:" + objectId;
    }

    public static String prefix() {
        return PREFIX;
    }
}
