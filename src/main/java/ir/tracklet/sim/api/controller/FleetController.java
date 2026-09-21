package ir.tracklet.sim.api.controller;

import ir.tracklet.sim.api.dto.LiveStateObjectResponse;
import ir.tracklet.sim.api.dto.LiveStateResponse;
import ir.tracklet.sim.domain.Fleet;
import ir.tracklet.sim.service.FleetCatalogService;
import ir.tracklet.sim.service.FleetSimulationService;
import ir.tracklet.sim.util.BoundingBox;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/public/fleets")
public class FleetController {
    private final FleetCatalogService fleetCatalogService;
    private final FleetSimulationService simulationService;

    @GetMapping
    public List<Fleet> listFleets() {
        return fleetCatalogService.listFleets();
    }

    @GetMapping("/{fleetId}")
    public Fleet getFleet(@PathVariable String fleetId) {
        return fleetCatalogService.getFleet(fleetId);
    }

    @GetMapping("/{fleetId}/live-state")
    public ResponseEntity<LiveStateResponse> liveState(@PathVariable String fleetId, @RequestParam String bbox) {
        fleetCatalogService.getFleet(fleetId);
        BoundingBox box = BoundingBox.parse(bbox);
        List<LiveStateObjectResponse> objects = simulationService
                .liveState(fleetId, box.minLon(), box.minLat(), box.maxLon(), box.maxLat())
                .stream()
                .map(LiveStateObjectResponse::from)
                .toList();
        return ResponseEntity.ok(new LiveStateResponse(objects.size(), objects));
    }
}
