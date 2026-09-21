package ir.tracklet.sim.domain;

import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Data
public class TrackingObject {
    private String id;
    private String externalId;
    private String fleetId;
    private FleetType fleetType;
    private double lat;
    private double lon;
    private double alt;
    private double heading;
    private double groundSpeed;
    private double verticalSpeed;
    private String sourceId;
    private long version;
    private ObjectStatus status;
    private Instant lastObservedAt;
    private Instant lastReceivedAt;
    private String icao;
    private String callsign;
    private Integer squawk;
    private String source;
    private final Map<String, Object> attributes = new HashMap<>();

    public TrackingObject copy() {
        TrackingObject copy = new TrackingObject();
        copy.id = id;
        copy.externalId = externalId;
        copy.fleetId = fleetId;
        copy.fleetType = fleetType;
        copy.lat = lat;
        copy.lon = lon;
        copy.alt = alt;
        copy.heading = heading;
        copy.groundSpeed = groundSpeed;
        copy.verticalSpeed = verticalSpeed;
        copy.sourceId = sourceId;
        copy.version = version;
        copy.status = status;
        copy.lastObservedAt = lastObservedAt;
        copy.lastReceivedAt = lastReceivedAt;
        copy.icao = icao;
        copy.callsign = callsign;
        copy.squawk = squawk;
        copy.source = source;
        copy.attributes.putAll(attributes);
        return copy;
    }

}
