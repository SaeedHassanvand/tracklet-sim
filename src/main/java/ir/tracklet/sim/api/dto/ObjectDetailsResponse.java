package ir.tracklet.sim.api.dto;

import ir.tracklet.sim.domain.ObjectStatus;
import ir.tracklet.sim.domain.TrackingObject;

import java.time.Instant;
import java.util.Map;

public record ObjectDetailsResponse(
        String id,
        String externalId,
        String fleetId,
        double lat,
        double lon,
        double heading,
        double alt,
        double groundSpeed,
        double verticalSpeed,
        String sourceId,
        long version,
        ObjectStatus status,
        Instant lastObservedAt,
        Instant lastReceivedAt,
        String icao,
        String callsign,
        Integer squawk,
        String source,
        Map<String, Object> attributes) {

    public static ObjectDetailsResponse from(TrackingObject object) {
        return new ObjectDetailsResponse(
                object.getId(), object.getExternalId(), object.getFleetId(), object.getLat(), object.getLon(),
                object.getHeading(), object.getAlt(), object.getGroundSpeed(), object.getVerticalSpeed(),
                object.getSourceId(), object.getVersion(), object.getStatus(), object.getLastObservedAt(),
                object.getLastReceivedAt(), object.getIcao(), object.getCallsign(), object.getSquawk(),
                object.getSource(), Map.copyOf(object.getAttributes()));
    }
}
