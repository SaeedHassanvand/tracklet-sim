package ir.tracklet.sim.api.controller;

import ir.tracklet.sim.api.dto.ObjectDetailsResponse;
import ir.tracklet.sim.api.dto.TrajectoryResponse;
import ir.tracklet.sim.service.FleetCatalogService;
import ir.tracklet.sim.service.FleetSimulationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/public/fleets/{fleetId}/objects")
public class ObjectController {
    private final FleetCatalogService fleetCatalogService;
    private final FleetSimulationService simulationService;

    @GetMapping("/{objectId}")
    public ResponseEntity<ObjectDetailsResponse> getObject(
            @PathVariable String fleetId,
            @PathVariable String objectId,
            HttpServletRequest request) {
        fleetCatalogService.getFleet(fleetId);
        ObjectDetailsResponse response = ObjectDetailsResponse.from(simulationService.getObject(fleetId, objectId));
        String etag = "\"" + response.version() + "\"";
        if (etag.equals(request.getHeader("If-None-Match"))) {
            return ResponseEntity.status(304).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).body(response);
    }

    @GetMapping("/{objectId}/trajectory")
    public TrajectoryResponse trajectory(
            @PathVariable String fleetId,
            @PathVariable String objectId) {
        fleetCatalogService.getFleet(fleetId);
        var points = simulationService.trajectory(fleetId, objectId);
        return new TrajectoryResponse(points.size(), points);
    }
}
