package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.util.ColorUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RayConeBuilderTest {
    @Test
    void buildsVersionIndependentRenderConfiguration() {
        IRayConeConfig config = RayConeBuilder.create()
                .range(18.0)
                .angle(42.0)
                .colorStops(ColorUtils.parseColorStops(Arrays.asList("FFFF0000", "FF00FF00")))
                .addLayer(1.0f, 0.2f, 0.05f)
                .noiseAmplitude(0.15)
                .raycast(true)
                .fog().enabled(true).sizeScale(1.3).color("80FFFFFF").end()
                .build();

        assertEquals(18.0, config.range(), 1.0E-9);
        assertEquals(42.0, config.angle(), 1.0E-9);
        assertEquals(2, config.colorStops().size());
        assertEquals(1, config.layerCount());
        assertTrue(config.coneRaycast());
        assertTrue(config.fog().enabled());
    }
}
