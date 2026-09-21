package ir.tracklet.sim.api.dto;

import ir.tracklet.sim.domain.ObjectStatus;
import ir.tracklet.sim.domain.TrackingObject;

import java.time.Instant;

public record LiveStateObjectResponse(
        String id,
        String externalId,
        double lat,
        double lon,
        double alt,
        double heading,
        double groundSpeed,
        ObjectStatus status,
        long version,
        Instant timestamp) {

    public static LiveStateObjectResponse from(TrackingObject object) {
        return new LiveStateObjectResponse(
                object.getId(), object.getExternalId(), object.getLat(), object.getLon(), object.getAlt(),
                object.getHeading(), object.getGroundSpeed(), object.getStatus(), object.getVersion(),
                object.getLastObservedAt());
    }
}
