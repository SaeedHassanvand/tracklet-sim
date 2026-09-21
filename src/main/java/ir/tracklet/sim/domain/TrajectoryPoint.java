package ir.tracklet.sim.domain;

import java.time.Instant;

public record TrajectoryPoint(
        double lat,
        double lon,
        double alt,
        Instant timestamp) {
}
