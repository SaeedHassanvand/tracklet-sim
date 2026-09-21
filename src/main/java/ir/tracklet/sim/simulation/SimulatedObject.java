package ir.tracklet.sim.simulation;

import ir.tracklet.sim.domain.TrackingObject;

import java.time.Instant;

public final class SimulatedObject {
    private final TrackingObject object;
    private final SimulationRuntime runtime;
    private Instant lastSimulationAt;
    private Instant lastSuccessfulObservationAt;

    public SimulatedObject(TrackingObject object, SimulationRuntime runtime, Instant now) {
        this.object = object;
        this.runtime = runtime;
        this.lastSimulationAt = now;
        this.lastSuccessfulObservationAt = now;
    }

    public TrackingObject getObject() { return object; }
    public SimulationRuntime getRuntime() { return runtime; }
    public Instant getLastSimulationAt() { return lastSimulationAt; }
    public void setLastSimulationAt(Instant value) { this.lastSimulationAt = value; }
    public Instant getLastSuccessfulObservationAt() { return lastSuccessfulObservationAt; }
    public void setLastSuccessfulObservationAt(Instant value) { this.lastSuccessfulObservationAt = value; }
}
