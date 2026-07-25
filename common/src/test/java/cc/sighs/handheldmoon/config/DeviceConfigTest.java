package cc.sighs.handheldmoon.config;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeviceConfigTest {
    @Test
    void clampsFullMoonLuminance() {
        assertEquals(0.0, new FullMoonDeviceConfig(true, -1.0, false).realLightLuminance());
        assertEquals(15.0, new FullMoonDeviceConfig(true, 20.0, false).realLightLuminance());
    }

    @Test
    void clampsLampValuesAndCopiesLists() {
        List<String> colors = new ArrayList<>(Collections.singletonList("FFFFFFFF"));
        LampDeviceConfig config = new LampDeviceConfig(
                100.0,
                2.0,
                colors,
                true,
                2.0,
                false,
                false,
                -2.0,
                Arrays.asList("1.0"),
                Arrays.asList("0.1"),
                Arrays.asList("0.0"),
                Collections.emptyList(),
                5.0,
                new LampDeviceConfig.FogSettings(true, 3.0, -1.0, 2.0, "80FFFFFF")
        );

        colors.clear();

        assertEquals(64.0, config.lightRange());
        assertEquals(10.0, config.lightAngle());
        assertEquals(1.0, config.lightIntensity());
        assertEquals(0.0, config.realLightLuminance());
        assertEquals(1.0, config.colorNoiseAmplitude());
        assertEquals(Collections.singletonList("FFFFFFFF"), config.lightColorsARGB());
        assertThrows(UnsupportedOperationException.class, () -> config.lightColorsARGB().add("FF000000"));
        assertEquals(2.0, config.fog().sizeScale());
        assertEquals(0.0, config.fog().centerAlpha());
        assertEquals(1.0, config.fog().edgeAlpha());
    }
}
