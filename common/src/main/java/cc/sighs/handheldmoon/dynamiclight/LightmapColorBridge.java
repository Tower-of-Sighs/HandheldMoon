package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.client.renderer.state.LightmapRenderState;

/** CPU mirror of vanilla 26.1 lightmap.fsh, published immutably for mesh workers. */
public final class LightmapColorBridge {
    private static volatile double[] table;
    private static volatile boolean changed;
    private LightmapColorBridge() { }

    public static void update(LightmapRenderState s) {
        if (!s.needsUpdate) return;
        double[] next = new double[16 * 16 * 3];
        for (int sky = 0; sky < 16; sky++) for (int block = 0; block < 16; block++) {
            double b = block / 15.0, k = sky / 15.0;
            double bb = b / (4 - 3 * b) * s.blockFactor;
            double sb = k / (4 - 3 * k) * s.skyFactor;
            double tintMix = 0.9 * (2 * b - 1) * (2 * b - 1);
            int offset = (sky * 16 + block) * 3;
            double max = 0;
            for (int c = 0; c < 3; c++) {
                double ambient = Math.max(s.ambientColor.get(c), s.nightVisionColor.get(c) * s.nightVisionEffectIntensity);
                double value = ambient + s.skyLightColor.get(c) * sb
                        + (s.blockLightTint.get(c) * (1 - tintMix) + tintMix) * bb;
                value *= 1 - s.bossOverlayWorldDarkening * (c == 0 ? 0.3 : 0.4);
                next[offset + c] = Math.clamp(value - s.darknessEffectScale, 0, 1);
                max = Math.max(max, next[offset + c]);
            }
            double gammaScale = max > 0 ? (1 - Math.pow(1 - max, 4)) / max : 1;
            for (int c = 0; c < 3; c++) next[offset + c] *= 1 - s.brightness + s.brightness * gammaScale;
        }
        double[] previous = table;
        double delta = 0;
        if (previous != null) for (int i = 0; i < next.length; i++) delta = Math.max(delta, Math.abs(next[i] - previous[i]));
        // Coalesce small torch flicker; significant environment changes invalidate baked coefficients.
        if (previous == null || delta >= 0.02) { table = next; changed = true; }
    }

    public static boolean consumeChanged() {
        if (!changed) return false;
        changed = false;
        return true;
    }

    public static int coefficient(double dynamic, double vanilla, double sky, int color) {
        double[] current = table;
        if (current == null || dynamic <= vanilla) return -1;
        double alpha = ((color >>> 24) & 255) / 255.0;
        int result = 0xFF000000;
        for (int c = 0; c < 3; c++) {
            double background = sample(current, vanilla, sky, c);
            double lit = sample(current, dynamic, sky, c);
            double channel = ((color >>> (16 - c * 8)) & 255) / 255.0;
            result |= composeChannel(background, lit, channel, alpha) << (16 - c * 8);
        }
        return result;
    }

    /** Bilinear filtering at texel centers, matching sample_lightmap.glsl. */
    static double sample(double[] data, double block, double sky, int channel) {
        block = Math.clamp(block, 0, 15); sky = Math.clamp(sky, 0, 15);
        int b = (int) block, k = (int) sky;
        int b1 = Math.min(b + 1, 15), k1 = Math.min(k + 1, 15);
        double u = block - b, v = sky - k;
        return data[(k * 16 + b) * 3 + channel] * (1-u)*(1-v)
                + data[(k * 16 + b1) * 3 + channel] * u*(1-v)
                + data[(k1 * 16 + b) * 3 + channel] * (1-u)*v
                + data[(k1 * 16 + b1) * 3 + channel] * u*v;
    }

    static int composeChannel(double background, double lit, double channel, double alpha) {
        if (lit <= 0 || lit <= background) return 255;
        double target = background + (lit - background) * (1 - alpha + alpha * channel);
        // Round upward so quantization cannot take the result below background.
        return Math.clamp((int) Math.ceil(255 * target / lit), 0, 255);
    }
}
