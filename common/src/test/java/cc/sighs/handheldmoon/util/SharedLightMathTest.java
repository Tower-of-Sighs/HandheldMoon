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
        assertEquals(7.5, SharedLightMath.pointLight(
                0.5, 0.5, 0.5, 10.0, 7, 0, 0, 14.0
        ), 1.0E-9);
        assertEquals(0.75, SharedLightMath.coneLight(
                0.5, 0.5, 0.5, 0.0, 0.0, 1.0,
                1.0, 0, 0, 7, 14.0,
                Math.cos(0.2), Math.cos(0.5), Math.cos(0.5) * Math.cos(0.5)
        ), 1.0E-9);
        assertEquals(24L, SharedLightMath.volume(0, 0, 0, 1, 2, 3));
    }

    @Test
    void computesSphericalConeBoundsWithoutDroppingTheApex() {
        double halfAngle = Math.toRadians(30.0);
        SharedLightMath.Aabb bounds = SharedLightMath.sphericalConeBounds(
                0.0, 0.0, 0.0,
                0.0, 0.0, 1.0,
                10.0, halfAngle
        );

        assertEquals(-5.0, bounds.minX(), 1.0E-9);
        assertEquals(-5.0, bounds.minY(), 1.0E-9);
        assertEquals(0.0, bounds.minZ(), 1.0E-9);
        assertEquals(5.0, bounds.maxX(), 1.0E-9);
        assertEquals(5.0, bounds.maxY(), 1.0E-9);
        assertEquals(10.0, bounds.maxZ(), 1.0E-9);
    }

    @Test
    void usesAFullSphereForDegenerateConeDirections() {
        SharedLightMath.Aabb bounds = SharedLightMath.sphericalConeBounds(
                1.25, -2.5, 3.75,
                0.0, 0.0, 0.0,
                4.0, Math.toRadians(20.0)
        );

        assertEquals(-2.75, bounds.minX(), 1.0E-9);
        assertEquals(-6.5, bounds.minY(), 1.0E-9);
        assertEquals(-0.25, bounds.minZ(), 1.0E-9);
        assertEquals(5.25, bounds.maxX(), 1.0E-9);
        assertEquals(1.5, bounds.maxY(), 1.0E-9);
        assertEquals(7.75, bounds.maxZ(), 1.0E-9);
    }
}
