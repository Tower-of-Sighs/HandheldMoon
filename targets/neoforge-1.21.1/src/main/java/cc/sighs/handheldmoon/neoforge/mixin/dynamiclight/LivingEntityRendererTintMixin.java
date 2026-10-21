package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.neoforge.compat.EntityTintContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Marks the entity being rendered so model cubes can sample dynamic light. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererTintMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void handheldmoon$beginTint(
            LivingEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            CallbackInfo callback
    ) {
        EntityTintContext.begin(entity);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void handheldmoon$endTint(
            LivingEntity entity, float entityYaw, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
            CallbackInfo callback
    ) {
        EntityTintContext.end();
    }
}
