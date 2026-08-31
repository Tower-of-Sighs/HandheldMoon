package cc.sighs.handheldmoon.fabric.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockModelLighter.class, priority = 900)
public abstract class DynamicLightBlockModelLighterMixin {
    @Inject(method = "prepareQuadFlat", at = @At("TAIL"))
    private void handheldmoon$tintFlatQuad(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            int lightCoords,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        outputInstance.multiplyColor(DynamicLightRenderHelper.tintCoefficient(pos));
    }

    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("TAIL"))
    private void handheldmoon$tintAoQuad(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        outputInstance.multiplyColor(DynamicLightRenderHelper.tintCoefficient(pos));
    }
}
