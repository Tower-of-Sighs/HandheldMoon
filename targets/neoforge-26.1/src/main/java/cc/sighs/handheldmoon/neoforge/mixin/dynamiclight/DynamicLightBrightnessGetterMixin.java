package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelRenderer.BrightnessGetter.class, priority = 900)
public interface DynamicLightBrightnessGetterMixin {
    @Inject(method = "lambda$static$0", at = @At("TAIL"), cancellable = true)
    private static void handheldmoon$applyDynamicBlockLight(
            BlockAndLightGetter level,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (!level.getBlockState(pos).isSolidRender()) {
            cir.setReturnValue(DynamicLightRenderHelper.apply(pos, cir.getReturnValueI()));
        }
    }
}
