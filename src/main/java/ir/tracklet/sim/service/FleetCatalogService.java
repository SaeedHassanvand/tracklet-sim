package ir.tracklet.sim.service;

import ir.tracklet.sim.config.SimulatorProperties;
import ir.tracklet.sim.domain.Fleet;
import ir.tracklet.sim.domain.FleetType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FleetCatalogService {
    private final SimulatorProperties properties;

    public FleetCatalogService(SimulatorProperties properties) {
        this.properties = properties;
    }

    public List<Fleet> listFleets() {
        return List.of(
                fleet(FleetType.AERIAL, "Aerial Fleet"),
                fleet(FleetType.BRT, "BRT Bus Fleet"),
                fleet(FleetType.MOTORCYCLE, "Motorcycle Courier Fleet")
        );
    }

    public Fleet getFleet(String fleetId) {
        FleetType type = parseFleetType(fleetId);
        return fleet(type, displayName(type));
    }

    public FleetType parseFleetType(String fleetId) {
        try {
            return FleetType.valueOf(fleetId.replace('-', '_').toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown fleet: " + fleetId);
        }
    }

    private Fleet fleet(FleetType type, String name) {
        SimulatorProperties.FleetConfig config = properties.getFleets().get(type);
        return new Fleet(type.name().toLowerCase(), name, type,
                config != null && config.isEnabled(),
                config == null ? 0 : config.getObjectCount());
    }

    private String displayName(FleetType type) {
        return switch (type) {
            case AERIAL -> "Aerial Fleet";
            case BRT -> "BRT Bus Fleet";
            case MOTORCYCLE -> "Motorcycle Courier Fleet";
        };
    }
}
