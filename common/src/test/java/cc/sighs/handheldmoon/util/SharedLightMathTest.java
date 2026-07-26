package cc.sighs.handheldmoon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedLightMathTest {
    @Test
    void computesDirectionAndGeometryWithoutMinecraftTypes() {
        SharedLightMath.Direction direction = SharedLightMath.direction(0.0, 1.0, 0.0, 1.0, true);

        assertEquals(0.0, direction.x(), 1.0E-9);
        assertEquals(0.0, direction.y(), 1.0E-9);
        assertEquals(1.0, direction.z(), 1.0E-9);
        assertEquals(5.0, SharedLightMath.effectiveRange(2.0, 10.0, 1.5), 1.0E-9);
        assertEquals(1.0, SharedLightMath.distanceAttenuation(0.0, 14.0), 1.0E-9);
        assertEquals(0.75, SharedLightMath.distanceAttenuation(7.0, 14.0), 1.0E-9);
        assertEquals(0.26530612244897955, SharedLightMath.distanceAttenuation(12.0, 14.0), 1.0E-9);
        assertEquals(0.0, SharedLightMath.distanceAttenuation(14.0, 14.0), 1.0E-9);
        assertEquals(24L, SharedLightMath.volume(0, 0, 0, 1, 2, 3));
    }
}
