package ir.tracklet.sim.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundingBoxTest {
    @Test
    void parsesStandardBbox() {
        BoundingBox box = BoundingBox.parse("51.20,35.60,51.60,35.85");
        assertEquals(51.20, box.minLon());
        assertEquals(35.60, box.minLat());
        assertEquals(51.60, box.maxLon());
        assertEquals(35.85, box.maxLat());
    }

    @Test
    void rejectsReversedBbox() {
        assertThrows(IllegalArgumentException.class,
                () -> BoundingBox.parse("51.60,35.60,51.20,35.85"));
    }
}
