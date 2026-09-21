package ir.tracklet.sim.domain;

public record Fleet(
        String id,
        String name,
        FleetType type,
        boolean enabled,
        int configuredObjectCount) {
}
