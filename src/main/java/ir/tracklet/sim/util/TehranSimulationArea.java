package ir.tracklet.sim.util;

import java.util.concurrent.ThreadLocalRandom;

public final class TehranSimulationArea {
    public static final double MIN_LAT = 35.60;
    public static final double MAX_LAT = 35.85;
    public static final double MIN_LON = 51.20;
    public static final double MAX_LON = 51.60;

    private TehranSimulationArea() {}

    public static double[] randomPoint() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return new double[]{
                r.nextDouble(MIN_LAT, MAX_LAT),
                r.nextDouble(MIN_LON, MAX_LON)
        };
    }

    public static boolean contains(double lat, double lon) {
        return lat >= MIN_LAT && lat <= MAX_LAT && lon >= MIN_LON && lon <= MAX_LON;
    }
}
