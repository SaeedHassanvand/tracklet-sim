package ir.tracklet.sim.repository;

import ir.tracklet.sim.domain.ObjectStatus;
import ir.tracklet.sim.domain.TrackingObject;
import ir.tracklet.sim.domain.TrajectoryPoint;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Repository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class FleetStateRepository {

    /**
     * Mean Earth radius used for haversine calculations.
     */
    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public FleetStateRepository(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public void clearSimulatorData() {
        var keys = redis.keys(RedisKeys.prefix() + "*");
        if (keys != null && !keys.isEmpty()) {
            redis.delete(keys);
        }
    }

    public void saveLiveState(TrackingObject object) {
        try {
            String objectKey = RedisKeys.object(object.getFleetId(), object.getId());
            String json = objectMapper.writeValueAsString(object);
            redis.opsForValue().set(objectKey, json);
            redis.opsForGeo().add(
                    RedisKeys.geo(object.getFleetId()),
                    new Point(
                            object.getLon(),
                            object.getLat()),
                    object.getId());

        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to serialize tracking object: " + object.getId(), e);
        }
    }

    public void save(TrackingObject object, TrajectoryPoint point, int maxTrajectoryPoints) {
        try {
            saveLiveState(object);
            String trajectoryKey = RedisKeys.trajectory(object.getFleetId(), object.getId());
            String trajectoryPointJson =
                    objectMapper.writeValueAsString(point);
            redis.opsForList().rightPush(
                    trajectoryKey,
                    trajectoryPointJson);
            if (maxTrajectoryPoints > 0) {
                redis.opsForList().trim(trajectoryKey, -((long) maxTrajectoryPoints), -1);
            }

        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to serialize trajectory data for object: " + object.getId(), e);
        }
    }

    public Optional<TrackingObject> find(String fleetId, String objectId) {
        String value = redis.opsForValue().get(RedisKeys.object(fleetId, objectId));
        if (value == null) {
            return Optional.empty();
        }
        try {
            TrackingObject object = objectMapper.readValue(value, TrackingObject.class);
            return Optional.of(object);
        } catch (JacksonException e) {
            throw new IllegalStateException("Unable to deserialize tracking object: " + objectId, e);
        }
    }

    public List<TrackingObject> findByBoundingBox(
            String fleetId,
            double minLon,
            double minLat,
            double maxLon,
            double maxLat) {
        validateBoundingBox(
                minLon,
                minLat,
                maxLon,
                maxLat);
        GeoOperations<String, String> geo =
                redis.opsForGeo();

        double centerLon = (minLon + maxLon) / 2.0;
        double centerLat = (minLat + maxLat) / 2.0;
        double widthKm = haversineDistanceKm(
                        centerLat,
                        minLon,
                        centerLat,
                        maxLon);
        double heightKm =
                haversineDistanceKm(
                        minLat,
                        centerLon,
                        maxLat,
                        centerLon);
        BoundingBox redisBoundingBox =
                new BoundingBox(
                        widthKm,
                        heightKm,
                        Metrics.KILOMETERS);

        GeoReference<String> reference =
                GeoReference.fromCoordinate(
                        centerLon,
                        centerLat);
        var matches =
                geo.search(
                        RedisKeys.geo(fleetId),
                        reference,
                        redisBoundingBox);

        if (matches == null || matches.getContent().isEmpty()) {
            return Collections.emptyList();
        }

        List<TrackingObject> result = new ArrayList<>(matches.getContent().size());
        for (var match : matches.getContent()) {
            String objectId = match.getContent().getName();
            Optional<TrackingObject> object = find(fleetId, objectId);
            if (object.isEmpty()) {
                continue;
            }
            TrackingObject trackingObject = object.get();
            if (trackingObject.getStatus() == ObjectStatus.LOST) {
                continue;
            }
            double lon = trackingObject.getLon();
            double lat = trackingObject.getLat();

            if (lon >= minLon
                    && lon <= maxLon
                    && lat >= minLat
                    && lat <= maxLat) {
                result.add(trackingObject);
            }
        }

        return result;
    }

    public List<TrajectoryPoint> findTrajectory(String fleetId, String objectId) {
        List<String> values = redis.opsForList().range(RedisKeys.trajectory(fleetId, objectId), 0, -1);
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<TrajectoryPoint> points =
                new ArrayList<>(values.size());
        for (String value : values) {
            try {

                TrajectoryPoint point =
                        objectMapper.readValue(
                                value,
                                TrajectoryPoint.class);

                points.add(point);

            } catch (JacksonException e) {

                throw new IllegalStateException(
                        "Unable to deserialize trajectory point "
                                + "for object: "
                                + objectId,
                        e);
            }
        }

        return points;
    }

    /**
     * Returns whether an object has trajectory data.
     */
    public boolean trajectoryExists(
            String fleetId,
            String objectId) {

        Long size =
                redis.opsForList().size(
                        RedisKeys.trajectory(
                                fleetId,
                                objectId));

        return size != null && size > 0;
    }

    /**
     * Removes the object from the live-state store and
     * from the Redis GEO index.
     *
     * The trajectory is intentionally retained.
     */
    public void deleteLiveObject(
            TrackingObject object) {

        String fleetId =
                object.getFleetId();

        String objectId =
                object.getId();

        redis.delete(
                RedisKeys.object(
                        fleetId,
                        objectId));

        redis.opsForGeo().remove(
                RedisKeys.geo(fleetId),
                objectId);
    }

    /**
     * Validates geographic bbox coordinates.
     */
    private void validateBoundingBox(
            double minLon,
            double minLat,
            double maxLon,
            double maxLat) {

        if (minLon < -180.0 || minLon > 180.0) {
            throw new IllegalArgumentException(
                    "minLon must be between -180 and 180");
        }

        if (maxLon < -180.0 || maxLon > 180.0) {
            throw new IllegalArgumentException(
                    "maxLon must be between -180 and 180");
        }

        if (minLat < -90.0 || minLat > 90.0) {
            throw new IllegalArgumentException(
                    "minLat must be between -90 and 90");
        }

        if (maxLat < -90.0 || maxLat > 90.0) {
            throw new IllegalArgumentException(
                    "maxLat must be between -90 and 90");
        }

        if (minLon > maxLon) {
            throw new IllegalArgumentException(
                    "minLon must not be greater than maxLon");
        }

        if (minLat > maxLat) {
            throw new IllegalArgumentException(
                    "minLat must not be greater than maxLat");
        }
    }

    /**
     * Calculates the great-circle distance between two
     * latitude/longitude points using the haversine formula.
     *
     * Result is expressed in kilometers.
     */
    private double haversineDistanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2) {

        double lat1Rad =
                Math.toRadians(lat1);

        double lat2Rad =
                Math.toRadians(lat2);

        double deltaLatRad =
                Math.toRadians(lat2 - lat1);

        double deltaLonRad =
                Math.toRadians(lon2 - lon1);

        double sinLat =
                Math.sin(deltaLatRad / 2.0);

        double sinLon =
                Math.sin(deltaLonRad / 2.0);

        double a =
                sinLat * sinLat
                        + Math.cos(lat1Rad)
                        * Math.cos(lat2Rad)
                        * sinLon * sinLon;

        double c =
                2.0 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1.0 - a));

        return EARTH_RADIUS_KM * c;
    }
}