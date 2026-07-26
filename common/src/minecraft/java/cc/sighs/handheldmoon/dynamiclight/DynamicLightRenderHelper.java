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
}
