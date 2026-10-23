package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Feeds the dynamic block light into the packed light vanilla hands to chunk
 * meshing. 1.20.1 routes every block-light sample (flat, smooth and the
 * {@code ModelBlockRenderer.Cache}) through this method.
 */
@Mixin(value = LevelRenderer.class, priority = 900)
public abstract class DynamicLightBrightnessGetterMixin {
    @Inject(
            method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void handheldmoon$applyDynamicBlockLight(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (!state.isSolidRender(level, pos)) {
            cir.setReturnValue(DynamicLightRenderHelper.apply(pos, cir.getReturnValueI()));
        }
    }
}
