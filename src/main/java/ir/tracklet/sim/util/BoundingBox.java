package ir.tracklet.sim.util;

public record BoundingBox(double minLon, double minLat, double maxLon, double maxLat) {
    public static BoundingBox parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("bbox is required");
        }
        String[] parts = value.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("bbox must be minLon,minLat,maxLon,maxLat");
        }
        try {
            double minLon = Double.parseDouble(parts[0].trim());
            double minLat = Double.parseDouble(parts[1].trim());
            double maxLon = Double.parseDouble(parts[2].trim());
            double maxLat = Double.parseDouble(parts[3].trim());
            BoundingBox box = new BoundingBox(minLon, minLat, maxLon, maxLat);
            box.validate();
            return box;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bbox values must be valid numbers", e);
        }
    }

    private void validate() {
        if (minLon < -180 || minLon > 180 || maxLon < -180 || maxLon > 180) {
            throw new IllegalArgumentException("Longitude must be in [-180, 180]");
        }
        if (minLat < -90 || minLat > 90 || maxLat < -90 || maxLat > 90) {
            throw new IllegalArgumentException("Latitude must be in [-90, 90]");
        }
        if (minLon > maxLon || minLat > maxLat) {
            throw new IllegalArgumentException("bbox minimum values must not exceed maximum values");
        }
    }
}
