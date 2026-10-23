package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;

/** Packed-light bridge used by renderer mixins. */
public final class DynamicLightRenderHelper {
    /** Version-independent subset of a light getter used by the tint helpers. */
    @FunctionalInterface
    public interface LightSampler {
        int getBrightness(LightLayer layer, BlockPos pos);
    }

    private static final int SKY_MASK = 0xFFF00000;
    private static final int BLOCK_MASK = 0x000FFFFF;
    /** Block light coordinate of level 1. */
    private static final int SKIRT_COORD = 16;
    /** Level at which a cell can act as a smooth-lighting donor. */
    private static final double DONOR_LEVEL = 3.0;
    /**
     * Strength of the beam-edge taper. Vanilla smooth lighting averages the four
     * cells around every vertex, so a lit cell that only touches unlit cells hands
     * a quarter of its level to the bordering vertices. Near a cone apex the
     * footprint is narrower than one cell, so a single cell can carry the full
     * level while almost no area is lit; that is what reads as the bright spike.
     *
     * <p>Dimming is graded by the cell's lateral support instead of a yes/no
     * boundary test, because a binary test also dims the whole thin part of the
     * beam near the apex and makes the light look weak. A cell whose four
     * horizontal neighbours are properly lit keeps its level.</p>
     */
    private static final double EDGE_TAPER = 0.9;
    /**
     * Neighbour level that still counts as support. A neighbour below this is
     * treated as outside the beam, so a bright cell next to the faint ramp fringe
     * counts as an edge cell as well.
     */
    private static final double SUPPORT_LEVEL = 3.5;

    private DynamicLightRenderHelper() {
    }

    public static int apply(BlockPos pos, int packedLight) {
        double dynamic = DynamicLightManager.getLightLevel(pos);
        int vanillaBlock = (packedLight & 0xFFFF) >> 4;
        if (dynamic <= vanillaBlock) {
            return zeroFillSkirt(pos, packedLight, vanillaBlock);
        }
        int dynamicPacked = (int) (Math.min(dynamic, 15.0) * 16.0);
        double support = lateralSupport(pos);
        if (support < 1.0) {
            dynamicPacked = (int) (dynamicPacked * (1.0 - EDGE_TAPER * (1.0 - support)));
        }
        return (packedLight & SKY_MASK) | (dynamicPacked & BLOCK_MASK);
    }

    /**
     * Fraction of the four horizontal neighbours that count as beam support.
     * {@code 1} only when all four are properly lit, so the beam body keeps its
     * full level while the rim - and anything touching the faint fringe - dims.
     */
    private static double lateralSupport(BlockPos pos) {
        if (!DynamicLightManager.hasSourcesNear(pos)) {
            return 1.0;
        }
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int lit = 0;
        if (DynamicLightManager.getLightLevel(x + 1, y, z) >= SUPPORT_LEVEL) {
            lit++;
        }
        if (DynamicLightManager.getLightLevel(x - 1, y, z) >= SUPPORT_LEVEL) {
            lit++;
        }
        if (DynamicLightManager.getLightLevel(x, y, z + 1) >= SUPPORT_LEVEL) {
            lit++;
        }
        if (DynamicLightManager.getLightLevel(x, y, z - 1) >= SUPPORT_LEVEL) {
            lit++;
        }
        return lit / 4.0;
    }

    /**
     * Keeps vanilla's smooth-lighting zero-fill rule inert along a beam rim.
     *
     * <p>{@code LightCoordsUtil.smoothBlend} replaces a sample whose integer block
     * level is 0 with the diagonal sample's level whenever that sample is above 2.
     * The analytic cone puts such pairs along its whole rim, and the resulting
     * four-cell average stops being an honest average. Returning block level 1 for
     * the first cell outside the rim keeps every such sample non-zero, so the rule
     * cannot fire.</p>
     */
    private static int zeroFillSkirt(BlockPos pos, int packedLight, int vanillaBlock) {
        if (vanillaBlock > 0) {
            return packedLight;
        }
        if (!DynamicLightManager.hasSourcesNear(pos) || !hasDonorNeighbour(pos)) {
            return packedLight;
        }
        return (packedLight & SKY_MASK) | SKIRT_COORD;
    }

    private static boolean hasDonorNeighbour(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    if (DynamicLightManager.getLightLevel(x + dx, y + dy, z + dz) >= DONOR_LEVEL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Computes an ARGB multiply coefficient that tints a vertex by the dynamic
     * light color. The coefficient lerps from white (no change) to the light
     * color as brightness increases.
     */
    public static int tintCoefficient(LightSampler level, BlockPos pos) {
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
            LightSampler level, BlockPos lightPos, Direction face,
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
            LightSampler level, BlockPos pos, Direction face,
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
            LightSampler level, BlockPos pos, Direction.Axis axis, int u, int v
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
        float strength = (float) Math.max(0.0, Math.min(1.0, contribution * alpha));
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
