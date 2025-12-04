package com.sighs.handheldmoon.mixin.lamb;

import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DynamicLightingEngine.class, remap = false)
public class DynamicLightingEngineMixin {
    @Mutable
    @Final
    private static int MAX_RADIUS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void handheldmoon$raiseRadius(CallbackInfo ci) {
        MAX_RADIUS = 32;
    }
}