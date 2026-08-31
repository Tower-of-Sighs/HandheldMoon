package cc.sighs.handheldmoon.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorUtilsTest {
    @Test
    void parsesAndNormalizesWebColorWithJavaColorDecoder() {
        assertEquals("#40A0FF80", ColorUtils.normalizeWebColor("#40A0FF80"));
        assertEquals("#40A0FFFF", ColorUtils.normalizeWebColor("40A0FF"));
        assertEquals("#40A0FF80", ColorUtils.normalizeWebColor("0x40A0FF80"));
        assertEquals("#40A0FF80", ColorUtils.argbToWebColor("8040A0FF"));

        float[] rgba = ColorUtils.parseColorRGBAWithAlpha("40A0FF80");
        assertEquals(64.0f / 255.0f, rgba[0], 1.0E-6f);
        assertEquals(160.0f / 255.0f, rgba[1], 1.0E-6f);
        assertEquals(1.0f, rgba[2], 1.0E-6f);
        assertEquals(128.0f / 255.0f, rgba[3], 1.0E-6f);
    }

    @Test
    void keepsInvalidColorStopsOutOfTheGradient() {
        // "FF0000FF" is a valid RRGGBBAA red; the invalid token is dropped.
        assertEquals(1, ColorUtils.parseColorStops(java.util.List.of(
                "not-a-color", "FF0000FF")).size());
    }

    @Test
    void convertsWebColorToCanonicalRgbaHex() {
        assertEquals("FFFFFF22", ColorUtils.webColorToRgbaHex("#FFFFFF22"));
        assertEquals("FFFFFF80", ColorUtils.webColorToRgbaHex("FFFFFF80"));
        assertEquals("40A0FF80", ColorUtils.webColorToRgbaHex("#40A0FF80"));
    }
}
