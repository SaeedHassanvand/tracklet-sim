package ir.tracklet.sim.simulation;

public final class SimulationRuntime {
    private double routePhase;
    private double turnRate;
    private int routeIndex;
    private final long seed;

    public SimulationRuntime(long seed) {
        this.seed = seed;
    }

    public double getRoutePhase() { return routePhase; }
    public void setRoutePhase(double routePhase) { this.routePhase = routePhase; }
    public double getTurnRate() { return turnRate; }
    public void setTurnRate(double turnRate) { this.turnRate = turnRate; }
    public int getRouteIndex() { return routeIndex; }
    public void setRouteIndex(int routeIndex) { this.routeIndex = routeIndex; }
    public long getSeed() { return seed; }
}
