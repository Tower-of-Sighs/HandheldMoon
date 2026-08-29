package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EntityRenderer.class, priority = 900)
public abstract class DynamicLightEntityRendererMixin {
    @Inject(method = "getBlockLightLevel", at = @At("RETURN"), cancellable = true)
    private void handheldmoon$applyDynamicEntityLight(
            Entity entity,
            BlockPos pos,
            CallbackInfoReturnable<Integer> cir
    ) {
        int dynamic = (int) DynamicLightManager.getLightLevel(pos);
        if (dynamic > cir.getReturnValueI()) {
            cir.setReturnValue(Math.min(dynamic, 15));
        }
    }
}
