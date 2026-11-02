package cc.sighs.handheldmoon.dynamiclight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeamEdgeLightingTest {
    @Test
    void beamInteriorKeepsFullBrightnessAndSkyLight() {
        assertEquals(0x00E000F0, BeamEdgeLighting.apply(0, 0, 0, 0x00E00000, (x, y, z) -> 15));
    }

    @Test
    void rimDimsGraduallyAsLateralSupportDisappears() {
        int previous = 0;
        for (int support = 0; support <= 4; support++) {
            final int neighbours = support;
            int result = BeamEdgeLighting.apply(0, 0, 0, 0, (x, y, z) -> {
                if (x == 0 && y == 0 && z == 0) return 15;
                if (y != 0) return 0;
                if (x == 1 && z == 0 && neighbours >= 1) return 15;
                if (x == -1 && z == 0 && neighbours >= 2) return 15;
                if (x == 0 && z == 1 && neighbours >= 3) return 15;
                if (x == 0 && z == -1 && neighbours >= 4) return 15;
                return 0;
            });
            assertTrue(result > previous);
            previous = result;
        }
        assertEquals(240, previous);
    }

    @Test
    void diagonalDonorAddsOnlyOneLevelOutsideRim() {
        int sky = 0x00D00000;
        assertEquals(sky | 16, BeamEdgeLighting.apply(-16, 5, 15, sky,
                (x, y, z) -> x == -17 && y == 6 && z == 16 ? 3 : 0));
        assertEquals(sky, BeamEdgeLighting.apply(-16, 5, 15, sky,
                (x, y, z) -> x == -18 && y == 6 && z == 16 ? 15 : 0));
        assertEquals(sky, BeamEdgeLighting.apply(0, 0, 0, sky, (x, y, z) -> 0));
    }

    @Test
    void taperNeverReducesVanillaOrFractionalLight() {
        for (int vanilla = 1; vanilla <= 240; vanilla++) {
            int packed = 0x00F00000 | vanilla;
            int result = BeamEdgeLighting.apply(0, 0, 0, packed,
                    (x, y, z) -> x == 0 && y == 0 && z == 0 ? 15 : 0);
            assertTrue((result & 65535) >= vanilla);
            assertEquals(packed & 0xFFFF0000, result & 0xFFFF0000);
        }
        assertEquals(0x00B00007, BeamEdgeLighting.apply(0, 0, 0, 0x00B00007, (x, y, z) -> 0));
    }
}
