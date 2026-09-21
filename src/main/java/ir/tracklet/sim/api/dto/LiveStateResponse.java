package ir.tracklet.sim.api.dto;

import java.util.List;

public record LiveStateResponse(int size, List<LiveStateObjectResponse> objects) {
}
