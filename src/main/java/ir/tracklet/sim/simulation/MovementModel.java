package ir.tracklet.sim.simulation;

import ir.tracklet.sim.domain.TrackingObject;

import java.time.Duration;

public interface MovementModel {
    void update(TrackingObject object, SimulationRuntime runtime, Duration delta);
}
