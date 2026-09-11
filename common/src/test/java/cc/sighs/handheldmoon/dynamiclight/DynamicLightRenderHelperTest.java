package cc.sighs.handheldmoon.dynamiclight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicLightRenderHelperTest {
    @Test
    void fractionalVertexLightUsesFilteredLightmapInsteadOfNeighborBrightness() {
        double[] table = new double[16 * 16 * 3];
        for (int sky = 0; sky < 16; sky++) for (int block = 0; block < 16; block++)
            for (int c = 0; c < 3; c++) table[(sky * 16 + block) * 3 + c] = 0.02 + block * 0.04 + sky * 0.001;
        double previous = 256;
        for (double block : new double[]{0, 0.5, 3.5, 7.5}) {
            double ambient = LightmapColorBridge.sample(table, 0, 14.75, 1);
            double lit = LightmapColorBridge.sample(table, block, 14.75, 1);
            assertEquals(0.02 + block * 0.04 + 14.75 * 0.001, lit, 1e-10);
            int coefficient = LightmapColorBridge.composeChannel(ambient, lit, 0, 1);
            assertTrue(coefficient <= previous);
            assertTrue(lit * coefficient / 255.0 >= ambient - 1e-10);
            previous = coefficient;
        }
    }

    @Test
    void coloredContributionNeverRemovesAmbientLight() {
        for (double background : new double[]{0, 0.02, 0.2, 0.8}) {
            for (double lit : new double[]{0.01, 0.1, 0.5, 1}) {
                if (lit < background) continue;
                int coefficient = LightmapColorBridge.composeChannel(background, lit, 0, 1);
                assertTrue(lit * coefficient / 255.0 + 1e-9 >= background);
            }
        }
        assertEquals(255, LightmapColorBridge.composeChannel(0.1, 0.1, 0, 1));
        assertEquals(255, LightmapColorBridge.composeChannel(0.1, 0.9, 1, 1));
        assertEquals(255, LightmapColorBridge.composeChannel(0.1, 0.9, 0, 0));
        assertEquals(0, LightmapColorBridge.composeChannel(0, 1, 0, 1));
    }

    @Test
    void capturedSourceTipKeepsRedWhenOtherSamplesAreUnlit() {
        var dark = new DynamicLightRenderHelper.TintSample(0, -1);
        var red = new DynamicLightRenderHelper.TintSample(14.985204696655273, 0xFFFF0000);
        assertEquals(0xFFFF0000, DynamicLightRenderHelper.interpolateLitTint(
                dark, dark, dark, red, 0.84865183, 0.65510519));
        assertEquals(-1, DynamicLightRenderHelper.interpolateLitTint(dark, dark, dark, dark, 0.5, 0.5));
    }

    @Test
    void actualWhiteLightStillDilutesRedInProportionToItsEnergy() {
        var white = new DynamicLightRenderHelper.TintSample(5, -1);
        var red = new DynamicLightRenderHelper.TintSample(15, 0xFFFF0000);
        assertEquals(0xFFFF4040, DynamicLightRenderHelper.interpolateLitTint(
                white, red, white, red, 0.5, 0.5));
    }

    @Test
    void respectsColorAlphaAtFullTintStrength() {
        assertEquals(0xFFFFCCCC,
                DynamicLightRenderHelper.tintCoefficient(4.0, 0.0, 0x33FF0000));
    }

    @Test
    void scalesTintByDynamicContributionOverVanillaLight() {
        assertEquals(0xFFFF7F7F,
                DynamicLightRenderHelper.tintCoefficient(10.0, 5.0, 0xFFFF0000));
    }

    @Test
    void leavesVerticesUntintedWhenVanillaLightWinsOrColorIsTransparent() {
        assertEquals(-1,
                DynamicLightRenderHelper.tintCoefficient(5.0, 6.0, 0xFFFF0000));
        assertEquals(-1,
                DynamicLightRenderHelper.tintCoefficient(15.0, 0.0, 0x00FF0000));
    }
}
