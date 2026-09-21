package ir.tracklet.sim.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoUtilsTest {
    @Test
    void headingIsNormalized() {
        assertEquals(10.0, GeoUtils.normalizeHeading(370), 1e-9);
        assertEquals(350.0, GeoUtils.normalizeHeading(-10), 1e-9);
    }

    @Test
    void movementProducesDifferentPosition() {
        double lat = GeoUtils.moveLatitude(35.7, 1, 90);
        double lon = GeoUtils.moveLongitude(35.7, 51.3, 1, 90);
        assertNotEquals(35.7, lat);
        assertNotEquals(51.3, lon);
    }
}
