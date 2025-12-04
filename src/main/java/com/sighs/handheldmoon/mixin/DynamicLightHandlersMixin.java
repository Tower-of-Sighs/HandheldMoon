package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.entity.FullMoonEntity;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DynamicLightHandlers.class, remap = false)
public class DynamicLightHandlersMixin {
    @Inject(method = "canLightUp(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void enforce1(T entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof FullMoonEntity) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getLuminanceFrom(Lnet/minecraft/world/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void enforce2(T entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof FullMoonEntity) {
            cir.setReturnValue(15);
        }
    }
}
