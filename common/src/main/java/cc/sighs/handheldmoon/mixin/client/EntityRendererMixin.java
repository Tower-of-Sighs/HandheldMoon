package cc.sighs.handheldmoon.mixin.client;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    /**
     * Adds the dynamic light level to the entity's packed light. Sampling the
     * whole body (not just the eye probe) keeps entities standing in a beam as
     * bright as the lit blocks around them.
     */
    @Inject(method = "getPackedLightCoords", at = @At("RETURN"), cancellable = true)
    private void handheldmoon$applyDynamicLight(
            Entity entity, float partialTick, CallbackInfoReturnable<Integer> cir
    ) {
        int packed = cir.getReturnValueI();
        BlockPos probe = BlockPos.containing(entity.getLightProbePosition(partialTick));
        int block = Math.max(LightCoordsUtil.block(packed), DynamicLightManager.entityLight(entity, probe));
        if (entity instanceof Player player && Utils.isUsingFlashlight(player)) {
            block = 15;
        }
        cir.setReturnValue(LightCoordsUtil.withBlock(packed, block));
    }
}
