package ir.tracklet.sim.simulation;

import java.util.List;

public final class UrbanRouteCatalog {
    private UrbanRouteCatalog() {}

    public static List<TehranRoute> routes() {
        return List.of(
                new TehranRoute("BRT-1", List.of(
                        new double[]{35.7722, 51.4200},
                        new double[]{35.7440, 51.4180},
                        new double[]{35.7100, 51.4140},
                        new double[]{35.6800, 51.4050},
                        new double[]{35.6500, 51.4100},
                        new double[]{35.6800, 51.4050},
                        new double[]{35.7100, 51.4140},
                        new double[]{35.7440, 51.4180}
                )),
                new TehranRoute("BRT-2", List.of(
                        new double[]{35.7600, 51.3500},
                        new double[]{35.7550, 51.3900},
                        new double[]{35.7400, 51.4300},
                        new double[]{35.7200, 51.4700},
                        new double[]{35.7000, 51.5100},
                        new double[]{35.7200, 51.4700},
                        new double[]{35.7400, 51.4300},
                        new double[]{35.7550, 51.3900}
                )),
                new TehranRoute("BRT-3", List.of(
                        new double[]{35.8000, 51.3000},
                        new double[]{35.7800, 51.3400},
                        new double[]{35.7600, 51.3800},
                        new double[]{35.7400, 51.4200},
                        new double[]{35.7200, 51.4600},
                        new double[]{35.7400, 51.4200},
                        new double[]{35.7600, 51.3800},
                        new double[]{35.7800, 51.3400}
                ))
        );
    }
}
