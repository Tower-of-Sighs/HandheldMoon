package cc.sighs.handheldmoon.dynamiclight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

/**
 * Client-only bridge that tints submitted entity models by the dynamic light
 * color, mirroring the block vertex tinting in {@link DynamicLightRenderHelper}.
 * Kept separate so the helper stays usable from classpath-limited tests.
 */
public final class DynamicLightEntityTint {
    private DynamicLightEntityTint() {
    }

    /**
     * Returns the dynamic-light tint for an entity model sampled at the body
     * center, or -1 when no dynamic source reaches the entity.
     */
    public static int entityTint(EntityRenderState state) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return -1;
        }
        double y = state.y + Math.max(0.5, state.boundingBoxHeight * 0.5);
        BlockPos pos = BlockPos.containing(state.x, y, state.z);
        double dynamic = DynamicLightManager.getLightLevel(pos);
        int vanilla = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        int color = DynamicLightManager.getLightColor(pos);
        // Use the lightmap-aware coefficient (same as smooth block vertices) so
        // non-light channels stay at their ambient background instead of being
        // zeroed by a raw multiplicative tint.
        return LightmapColorBridge.coefficient(dynamic, vanilla, sky, color);
    }

    /**
     * Applies {@link #entityTint} to a submitted model. Non-entity models
     * (items, block entities) are returned unchanged.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static SubmitNodeStorage.ModelSubmit<?> tintEntityModel(SubmitNodeStorage.ModelSubmit<?> submit) {
        if (!(submit.state() instanceof EntityRenderState state)) {
            return submit;
        }
        int coefficient = entityTint(state);
        if (coefficient == -1) {
            return submit;
        }
        int base = submit.tintedColor() == -1 ? 0xFFFFFFFF : submit.tintedColor();
        return new SubmitNodeStorage.ModelSubmit(
                submit.pose(), submit.model(), submit.state(),
                submit.lightCoords(), submit.overlayCoords(),
                DynamicLightRenderHelper.multiplyArgb(base, coefficient),
                submit.sprite(), submit.outlineColor(), submit.crumblingOverlay());
    }
}
