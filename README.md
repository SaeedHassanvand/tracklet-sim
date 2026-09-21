# Tracklet Fleet Tracking Simulator

A Java 21 / Spring Boot 4.1.0 simulator for three continuously moving fleets:

- Aerial targets across a coarse Iran boundary.
- BRT buses following simulated Tehran routes.
- Motorcycle couriers moving inside a Tehran simulation area.

Redis stores live object state, Redis GEO stores the spatial index, and Redis lists store bounded trajectories.

## Requirements

### Local execution

- Java 21+
- Maven 3.9+
- Redis 8+ (or a compatible Redis version)

### Docker execution

- Docker Engine or Docker Desktop
- Docker Compose v2 (`docker compose`)

## Run with Docker Compose (Recommended)

The Docker Compose setup runs both the Spring Boot application and Redis in separate containers on the same Docker network.

### 1. Build and start the application

From the project root (the directory containing `pom.xml`, `Dockerfile`, and `docker-compose.yml`), run:

```bash
docker compose up --build
```

To run in the background:

```bash
docker compose up --build -d
```

The application will be available at:

```text
http://localhost:8080
```

Redis will be accessible from the host at:

```text
localhost:6379
```

Inside Docker Compose, the application connects to Redis using the service hostname `redis`, not `localhost`.

The relevant environment configuration is:

```yaml
environment:
  REDIS_HOST: redis
  REDIS_PORT: 6379
  SERVER_PORT: 8080
```

### 2. Check container status

```bash
docker compose ps
```

### 3. View application logs

```bash
docker compose logs -f app
```

View Redis logs:

```bash
docker compose logs -f redis
```

View logs for all services:

```bash
docker compose logs -f
```

### 4. Test Redis connectivity

```bash
docker exec -it tracklet-sim-redis redis-cli ping
```

Expected output:

```text
PONG
```

### 5. Stop the containers

```bash
docker compose down
```

This stops and removes the containers while preserving the named Redis volume.

To remove the containers and delete the Redis data volume:

```bash
docker compose down -v
```

> Warning: `docker compose down -v` deletes the persisted Redis data.

### 6. Rebuild after source changes

After changing Java code or resources, rebuild the image:

```bash
docker compose up --build -d
```

To force a clean image rebuild:

```bash
docker compose build --no-cache

docker compose up -d
```

## Run locally with Redis in Docker

This workflow is useful during development when you want to run the Spring Boot application directly from IntelliJ IDEA and run only Redis in Docker.

Start Redis:

```bash
docker compose up -d redis
```

Run the application from IntelliJ IDEA or with Maven:

```bash
mvn spring-boot:run
```

The local application must connect to Redis using `localhost`:

```text
REDIS_HOST=localhost
REDIS_PORT=6379
```

## Run locally without Docker

Start a locally installed Redis instance, then run:

```bash
mvn spring-boot:run
```

Or build and run the JAR:

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

Get live objects inside a bounding box. The `bbox` order is **minLon,minLat,maxLon,maxLat**:

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
