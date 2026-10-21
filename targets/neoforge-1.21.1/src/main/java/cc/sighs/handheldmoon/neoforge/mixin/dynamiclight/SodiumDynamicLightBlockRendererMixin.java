package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import cc.sighs.handheldmoon.neoforge.compat.sodium.SodiumBlockRenderContextAccess;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies dynamic-light color to Sodium 0.6 chunk quads on 1.21.1. */
@Mixin(BlockRenderer.class)
public abstract class SodiumDynamicLightBlockRendererMixin {
    @Inject(method = "bufferQuad", at = @At("HEAD"))
    private void handheldmoon$tintQuad(
            MutableQuadViewImpl quad, float[] brightness, Material material, CallbackInfo callback
    ) {
        SodiumBlockRenderContextAccess access = (SodiumBlockRenderContextAccess) (Object) this;
        BlockAndTintGetter level = access.handheldmoon$level();
        BlockPos pos = access.handheldmoon$pos();
        if (level == null || pos == null) {
            return;
        }
        int coefficient = DynamicLightRenderHelper.tintCoefficient(level::getBrightness, pos);
        if (coefficient == -1) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            quad.color(i, DynamicLightRenderHelper.multiplyArgb(quad.color(i), coefficient));
        }
    }
}
