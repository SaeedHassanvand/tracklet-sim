# Tracklet Fleet Tracking Simulator

A Java 21 / Spring Boot 4.1.0 simulator for three continuously moving fleets:

- Aerial targets across a coarse Iran boundary.
- BRT buses following simulated Tehran routes.
- Motorcycle couriers moving inside a Tehran simulation area.

Redis stores live object state, Redis GEO stores the spatial index, and Redis lists store bounded trajectories.

## Requirements

- Java 21+
- Maven 3.9+
- Docker / Docker Compose

## Run Redis

```bash
docker compose up -d redis
```

## Run the application

```bash
mvn spring-boot:run
```

Or build a jar:

```bash
mvn clean package
java -jar target/tracklet-sim-0.0.1-SNAPSHOT.jar
```

The default application port is `8080`.

## APIs

List fleets:

```http
GET http://localhost:8080/api/v1/public/fleets
```

Get a fleet:

```http
GET http://localhost:8080/api/v1/public/fleets/aerial
```

Get live objects inside a bbox. The bbox order is **minLon,minLat,maxLon,maxLat**:

```http
GET http://localhost:8080/api/v1/public/fleets/aerial/live-state?bbox=51.20,35.60,51.60,35.85
```

Get object details:

```http
GET http://localhost:8080/api/v1/public/fleets/aerial/objects/{objectId}
```

The object-details endpoint supports `ETag` / `If-None-Match` using the object's version.

Get a trajectory:

```http
GET http://localhost:8080/api/v1/public/fleets/aerial/objects/{objectId}/trajectory
```

## Configuration

The simulator is controlled by `src/main/resources/application.yml`.

The important settings are:

```yaml
simulator:
  engine-tick-ms: 1000
  lifecycle-tick-ms: 1000
  trajectory-max-points: 300
  fleets:
    aerial:
      object-count: 100
      update-interval-ms: 1000
      observation-drop-rate: 0.02
    brt:
      object-count: 30
      update-interval-ms: 2000
      observation-drop-rate: 0.01
    motorcycle:
      object-count: 100
      update-interval-ms: 1000
      observation-drop-rate: 0.015
  lifecycle:
    stale-threshold: 10s
    lost-threshold: 30s
```

`observation-drop-rate` is useful for exercising `STALE` and `LOST` lifecycle transitions.

## Redis layout

```text
tracklet-sim:fleet:{fleetId}:object:{objectId}
tracklet-sim:fleet:{fleetId}:geo
tracklet-sim:fleet:{fleetId}:trajectory:{objectId}
```

The GEO key stores the current longitude/latitude for each live object. The object key stores the latest JSON state. The trajectory key stores the most recent bounded list of trajectory points.

## Notes

The simulator initializes a fresh object population at startup and clears only its own `tracklet-sim:*` Redis namespace. This prevents stale simulator state from a previous run from appearing in a new run.

The Iran and Tehran geometries are intentionally coarse simulation boundaries rather than authoritative GIS administrative/road data. The BRT routes are simulated polylines and do not represent real operational routes.
