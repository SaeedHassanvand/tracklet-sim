package ir.tracklet.sim.api.dto;

import ir.tracklet.sim.domain.TrajectoryPoint;

import java.util.List;

public record TrajectoryResponse(int size, List<TrajectoryPoint> path) {
}
