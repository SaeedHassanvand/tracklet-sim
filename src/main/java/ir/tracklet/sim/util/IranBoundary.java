package ir.tracklet.sim.util;

import java.util.concurrent.ThreadLocalRandom;

public final class IranBoundary {
    // Coarse polygon sufficient for simulation. It is deliberately conservative near the border.
    private static final double[][] POLYGON = {
            {44.02, 39.78}, {45.50, 38.30}, {46.60, 39.45}, {48.80, 38.40},
            {50.90, 37.50}, {53.10, 37.40}, {55.80, 37.40}, {60.00, 35.75},
            {61.90, 34.00}, {63.30, 31.00}, {61.90, 29.30}, {60.00, 28.00},
            {59.10, 25.05}, {57.00, 25.10}, {55.00, 26.20}, {52.20, 26.00},
            {50.20, 25.20}, {48.00, 28.00}, {46.70, 28.80}, {45.00, 30.00},
            {44.00, 31.50}, {44.02, 39.78}
    };

    private IranBoundary() {}

    public static boolean contains(double lon, double lat) {
        boolean inside = false;
        for (int i = 0, j = POLYGON.length - 1; i < POLYGON.length; j = i++) {
            double xi = POLYGON[i][0];
            double yi = POLYGON[i][1];
            double xj = POLYGON[j][0];
            double yj = POLYGON[j][1];
            boolean intersects = ((yi > lat) != (yj > lat))
                    && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi);
            if (intersects) inside = !inside;
        }
        return inside;
    }

    public static double[] randomPoint() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < 10_000; i++) {
            double lon = r.nextDouble(44.0, 63.4);
            double lat = r.nextDouble(25.0, 39.9);
            if (contains(lon, lat)) return new double[]{lat, lon};
        }
        return new double[]{32.4279, 53.6880};
    }
}
