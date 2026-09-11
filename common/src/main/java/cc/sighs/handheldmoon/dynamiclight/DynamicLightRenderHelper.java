package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;

/** Packed-light bridge used by renderer mixins. */
public final class DynamicLightRenderHelper {
    private static final int SKY_MASK = 0xFFF00000;
    private static final int BLOCK_MASK = 0x000FFFFF;

    private DynamicLightRenderHelper() {
    }

    public static int apply(BlockPos pos, int packedLight) {
        double dynamic = DynamicLightManager.getLightLevel(pos);
        int vanillaBlock = (packedLight & 0xFFFF) >> 4;
        if (dynamic <= vanillaBlock) {
            return packedLight;
        }
        int dynamicPacked = (int) (Math.min(dynamic, 15.0) * 16.0);
        return (packedLight & SKY_MASK) | (dynamicPacked & BLOCK_MASK);
    }

    /**
     * Computes an ARGB multiply coefficient that tints a vertex by the dynamic
     * light color. The coefficient lerps from white (no change) to the light
     * color as brightness increases.
     */
    public static int tintCoefficient(BlockAndTintGetter level, BlockPos pos) {
        return LightmapColorBridge.coefficient(DynamicLightManager.getLightLevel(pos),
                level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos),
                DynamicLightManager.getLightColor(pos));
    }

    public static int tintCoefficient(BlockPos pos, double vanillaBlockLight) {
        double dynamic = DynamicLightManager.getLightLevel(pos);
        int color = DynamicLightManager.getLightColor(pos);
        return tintCoefficient(dynamic, vanillaBlockLight, color);
    }

    /**
     * Uses the visible-face light sample without expanding the colored region.
     * A neighboring maximum is not an AO interpolation: applying it to the
     * entire face creates a discontinuous colored fringe outside the source.
     */
    public static int tintCoefficientForSurface(
            BlockPos pos, Direction face, double vanillaBlockLight
    ) {
        return tintCoefficient(pos, vanillaBlockLight);
    }

    /**
     * Interpolates face-plane samples at the actual vertex. Adjacent coplanar
     * faces therefore get the same coefficient at their shared vertices.
     * This is a color filter, not a replacement for the renderer's AO weights.
     */
    public static int tintCoefficientForVertex(
            BlockAndTintGetter level, BlockPos lightPos, Direction face,
            float x, float y, float z
    ) {
        Direction.Axis axis = face.getAxis();
        double u = (axis == Direction.Axis.X ? z : x) - 0.5;
        double v = (axis == Direction.Axis.Y ? z : y) - 0.5;
        int baseU = (int) Math.floor(u);
        int baseV = (int) Math.floor(v);
        TintSample c00 = sampleFaceTint(level, lightPos, axis, baseU, baseV);
        TintSample c10 = sampleFaceTint(level, lightPos, axis, baseU + 1, baseV);
        TintSample c01 = sampleFaceTint(level, lightPos, axis, baseU, baseV + 1);
        TintSample c11 = sampleFaceTint(level, lightPos, axis, baseU + 1, baseV + 1);
        return interpolateLitTint(c00, c10, c01, c11, u - baseU, v - baseV);
    }

    /** Use the renderer's final brightness; neighbors supply hue only. */
    public static int tintCoefficientForVertex(
            BlockAndTintGetter level, BlockPos pos, Direction face,
            float x, float y, float z, int renderedLight
    ) {
        Direction.Axis axis = face.getAxis();
        double u = (axis == Direction.Axis.X ? z : x) - 0.5;
        double v = (axis == Direction.Axis.Y ? z : y) - 0.5;
        int bu = (int) Math.floor(u), bv = (int) Math.floor(v);
        u -= bu; v -= bv;
        double vanilla = 0, energy = 0, red = 0, green = 0, blue = 0;
        for (int j = 0; j < 2; j++) for (int i = 0; i < 2; i++) {
            BlockPos sample = switch (axis) {
                case X -> pos.offset(0, bv + j, bu + i);
                case Y -> pos.offset(bu + i, 0, bv + j);
                case Z -> pos.offset(bu + i, bv + j, 0);
            };
            double weight = (i == 0 ? 1 - u : u) * (j == 0 ? 1 - v : v);
            double base = level.getBrightness(LightLayer.BLOCK, sample);
            vanilla += weight * base;
            double contribution = weight * Math.max(0, DynamicLightManager.getLightLevel(sample) - base);
            int color = DynamicLightManager.getLightColor(sample);
            double alpha = ((color >>> 24) & 255) / 255.0;
            energy += contribution;
            red += contribution * (255 - (255 - ((color >>> 16) & 255)) * alpha);
            green += contribution * (255 - (255 - ((color >>> 8) & 255)) * alpha);
            blue += contribution * (255 - (255 - (color & 255)) * alpha);
        }
        if (energy <= 0) return -1;
        int hue = 0xFF000000 | (int) Math.round(red / energy) << 16
                | (int) Math.round(green / energy) << 8 | (int) Math.round(blue / energy);
        return LightmapColorBridge.coefficient((renderedLight & 65535) / 16.0, vanilla,
                ((renderedLight >>> 16) & 65535) / 16.0, hue);
    }

    private static TintSample sampleFaceTint(
            BlockAndTintGetter level, BlockPos pos, Direction.Axis axis, int u, int v
    ) {
        BlockPos sample = switch (axis) {
            case X -> pos.offset(0, v, u);
            case Y -> pos.offset(u, 0, v);
            case Z -> pos.offset(u, v, 0);
        };
        double dynamic = DynamicLightManager.getLightLevel(sample);
        int vanilla = level.getBrightness(LightLayer.BLOCK, sample);
        return new TintSample(Math.max(dynamic, vanilla),
                tintCoefficient(level, sample));
    }

    record TintSample(double light, int coefficient) { }

    /** Normalize by contributed light, not the number of spatial samples.
     * Zero-light samples carry no white energy and must not dilute the hue.
     */
    static int interpolateLitTint(TintSample c00, TintSample c10, TintSample c01, TintSample c11, double u, double v) {
        double w00 = (1.0 - u) * (1.0 - v) * c00.light;
        double w10 = u * (1.0 - v) * c10.light;
        double w01 = (1.0 - u) * v * c01.light;
        double w11 = u * v * c11.light;
        double total = w00 + w10 + w01 + w11;
        if (total <= 0.0) return -1;
        int result = 0xFF000000;
        for (int shift = 0; shift <= 16; shift += 8) {
            int channel = (int) Math.round((
                    ((c00.coefficient >>> shift) & 255) * w00 + ((c10.coefficient >>> shift) & 255) * w10
                    + ((c01.coefficient >>> shift) & 255) * w01 + ((c11.coefficient >>> shift) & 255) * w11) / total);
            result |= channel << shift;
        }
        return result;
    }

    /**
     * Computes a tint from the part of the rendered block light that is
     * actually supplied by the dynamic source. The color alpha controls the
     * maximum tint amount; light brightness is still carried by the lightmap.
     */
    static int tintCoefficient(double dynamic, double vanillaBlockLight, int lightColor) {
        if (!Double.isFinite(dynamic) || dynamic <= Math.max(vanillaBlockLight, 0.0)) {
            return -1; // identity for ARGB multiply
        }
        float alpha = ((lightColor >>> 24) & 0xFF) / 255.0f;
        if (alpha <= 0.0f) {
            return -1;
        }
        double contribution = (dynamic - Math.max(vanillaBlockLight, 0.0)) / dynamic;
        float strength = (float) Math.clamp(contribution * alpha, 0.0, 1.0);
        int r = (int) (255 - (255 - ((lightColor >> 16) & 0xFF)) * strength);
        int g = (int) (255 - (255 - ((lightColor >> 8) & 0xFF)) * strength);
        int b = (int) (255 - (255 - (lightColor & 0xFF)) * strength);
        return 0xFF000000 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    /** Per-channel ARGB multiply (version-independent; identity when either is -1). */
    public static int multiplyArgb(int lhs, int rhs) {
        if (lhs == -1) {
            return rhs;
        }
        if (rhs == -1) {
            return lhs;
        }
        int a = ((lhs >>> 24) & 0xFF) * ((rhs >>> 24) & 0xFF) / 255;
        int r = ((lhs >> 16) & 0xFF) * ((rhs >> 16) & 0xFF) / 255;
        int g = ((lhs >> 8) & 0xFF) * ((rhs >> 8) & 0xFF) / 255;
        int b = (lhs & 0xFF) * (rhs & 0xFF) / 255;
        return a << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
}
