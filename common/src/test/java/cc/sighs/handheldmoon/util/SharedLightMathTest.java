package cc.sighs.handheldmoon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SharedLightMathTest {
    @Test
    void computesDirectionAndGeometryWithoutMinecraftTypes() {
        SharedLightMath.Direction direction = SharedLightMath.direction(0.0f, 0.0f, true);

        assertEquals(0.0, direction.x(), 1.0E-9);
        assertEquals(0.0, direction.y(), 1.0E-9);
        assertEquals(1.0, direction.z(), 1.0E-9);
        assertEquals(5.0, SharedLightMath.effectiveRange(2.0, 10.0, 1.5), 1.0E-9);
        assertEquals(24L, SharedLightMath.volume(0, 0, 0, 1, 2, 3));
    }
}
