package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SodiumVertexLightingTest {
    @AfterEach
    void resetLightmap() {
        LightmapColorBridge.publish(null);
    }

    static DynamicLightBehavior.BatchLightSnapshot beam(int color) {
        return new DynamicLightBehavior.BatchLightSnapshot(true, 0, 0, 0,
                0, 0, 1, 32, 15, Math.cos(0.5), Math.cos(0.7),
                Math.pow(Math.cos(0.7), 2), AttenuationCurve.QUADRATIC, color);
    }

    @Test
    void continuousBeamDoesNotBorrowLightFromAnAdjacentLitVoxel() {
        var beam = beam(0xFFFF0000);
        assertTrue(beam.lightAtVertex(0.1, 0, 0.5) > 14);
        assertEquals(0, beam.lightAtVertex(0.9, 0, 0.5));
        double previous = 0;
        for (int i = 0; i <= 20; i++) {
            double value = beam.lightAtVertex(1.4 - i * 0.02, 0, 2);
            assertTrue(value >= previous);
            previous = value;
        }
    }

    @Test
    void preservesFractionalFlashlightLightAndSkyWithoutArtificialSkirt() {
        int sky = 0x00D00000;
        assertEquals(sky | 120, SodiumVertexLighting.combine(sky, -1, 7.5, -1).lightmap());
        assertEquals(sky, SodiumVertexLighting.combine(sky, -1, 0, -1).lightmap());
        assertEquals(sky | 160, SodiumVertexLighting.combine(sky | 160, -1, 7.5, -1).lightmap());
    }

    @Test
    void redBeamKeepsWhiteAmbientAndExistingColoredLights() {
        double[] table = new double[16 * 16 * 3];
        for (int sky = 0; sky < 16; sky++) for (int block = 0; block < 16; block++)
            for (int c = 0; c < 3; c++) table[(sky * 16 + block) * 3 + c] = block / 15.0;
        LightmapColorBridge.publish(table);
        var vertex = SodiumVertexLighting.combine(80, -1, 10, 0xFFFF0000);
        assertEquals(0xFFFF8080, vertex.tint());
        assertEquals(0xFF808000, SodiumVertexLighting.combine(80, 0xFF00FF00, 10, 0xFFFF0000).tint());
        assertEquals(0xFF00FF00, SodiumVertexLighting.combine(160, 0xFF00FF00, 5, 0xFFFF0000).tint());
        assertEquals(-1, SodiumVertexLighting.combine(80, -1, 10, 0x00FF0000).tint());
    }

    @Test
    void bakedEmissionAndSkyAreMergedIndependently() {
        assertEquals(0x00F000A0, SodiumVertexLighting.maxLight(0x00F00040, 0x005000A0));
    }

    @Test
    void samplingScopeRestoresAfterFailureAndStaysOnTheMeshWorker() throws Exception {
        assertFalse(SodiumLightSampling.isBlockSample());
        boolean previous = SodiumLightSampling.beginBlockSamples();
        try {
            assertTrue(SodiumLightSampling.isBlockSample());
            var worker = java.util.concurrent.Executors.newSingleThreadExecutor();
            try {
                assertFalse(worker.submit(SodiumLightSampling::isBlockSample).get());
            } finally {
                worker.shutdownNow();
            }
            boolean nested = SodiumLightSampling.beginBlockSamples();
            try {
                throw new IllegalStateException("sample failed");
            } catch (IllegalStateException expected) {
                assertEquals("sample failed", expected.getMessage());
            } finally {
                SodiumLightSampling.endBlockSamples(nested);
            }
            assertTrue(SodiumLightSampling.isBlockSample());
        } finally {
            SodiumLightSampling.endBlockSamples(previous);
        }
        assertFalse(SodiumLightSampling.isBlockSample());
    }
}
