package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Combines Sodium's completed environment/AO pass with a continuous flashlight sample. */
public final class SodiumVertexLighting {
    private SodiumVertexLighting() { }

    public record Vertex(int lightmap, int tint) { }

    public static Vertex shade(
            DynamicLightRenderHelper.LightSampler level, BlockPos pos, BlockPos lightPos,
            Direction face, float x, float y, float z, int baseline, boolean smooth
    ) {
        int tint;
        boolean previous = SodiumLightSampling.beginBlockSamples();
        try {
            tint = smooth ? DynamicLightRenderHelper.tintCoefficientForVertex(
                    level, lightPos, face, x, y, z, baseline)
                    : DynamicLightRenderHelper.tintCoefficient(level, lightPos);
        } finally {
            SodiumLightSampling.endBlockSamples(previous);
        }
        // A tiny outward offset avoids selecting the solid side of a boundary.
        var beam = DynamicLightManager.sampleVertexLight(
                pos.getX() + (double) x + face.getStepX() * 1.0e-4,
                pos.getY() + (double) y + face.getStepY() * 1.0e-4,
                pos.getZ() + (double) z + face.getStepZ() * 1.0e-4);
        return combine(baseline, tint, beam.light(), beam.color());
    }

    static Vertex combine(int baseline, int tint, double beam, int color) {
        double block = (baseline & 0xFFFF) / 16.0;
        int packed = (baseline & 0xFFFF0000) | Math.max(baseline & 0xFFFF, (int) (Math.min(15, beam) * 16));
        int coefficient = LightmapColorBridge.coefficientOverTint(
                (packed & 0xFFFF) / 16.0, block, ((packed >>> 16) & 0xFFFF) / 16.0, color, tint);
        return new Vertex(packed, coefficient);
    }

    /** Preserve baked emission and sky light when Sodium merges per-vertex light. */
    public static int maxLight(int a, int b) {
        return Math.max(a & 0xFFFF, b & 0xFFFF)
                | Math.max((a >>> 16) & 0xFFFF, (b >>> 16) & 0xFFFF) << 16;
    }
}
