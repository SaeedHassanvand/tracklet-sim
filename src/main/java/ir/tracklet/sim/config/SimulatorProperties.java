package ir.tracklet.sim.config;

import ir.tracklet.sim.domain.FleetType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {
    private boolean enabled = true;
    private long engineTickMs = 1000;
    private long lifecycleTickMs = 1000;
    private int trajectoryMaxPoints = 300;
    private Map<FleetType, FleetConfig> fleets = new EnumMap<>(FleetType.class);
    private LifecycleConfig lifecycle = new LifecycleConfig();

    @Data
    public static class FleetConfig {
        private boolean enabled = true;
        private int objectCount = 10;
        private long updateIntervalMs = 1000;
        private double observationDropRate;
        private double minSpeedKmh;
        private double maxSpeedKmh;
        private double minAltitudeFt;
        private double maxAltitudeFt;
        private long networkDelayMs;

    }

    @Data
    public static class LifecycleConfig {
        private Duration staleThreshold = Duration.ofSeconds(10);
        private Duration lostThreshold = Duration.ofSeconds(30);

    }
}
