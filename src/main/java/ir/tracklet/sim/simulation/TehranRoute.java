package ir.tracklet.sim.simulation;

import java.util.List;

public record TehranRoute(String name, List<double[]> waypoints) {
    public double[] waypoint(int index) {
        return waypoints.get(index % waypoints.size());
    }
}
