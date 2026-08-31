package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.core.BlockPos;

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
    public static int tintCoefficient(BlockPos pos) {
        double dynamic = DynamicLightManager.getLightLevel(pos);
        if (dynamic <= 0.0) {
            return -1; // identity for ARGB multiply
        }
        int lightColor = DynamicLightManager.getLightColor(pos);
        float strength = (float) Math.min(dynamic, 15.0) / 15.0f;
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
