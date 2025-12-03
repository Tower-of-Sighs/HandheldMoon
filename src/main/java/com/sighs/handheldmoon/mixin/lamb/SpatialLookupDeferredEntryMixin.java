package com.sighs.handheldmoon.mixin.lamb;

import com.sighs.handheldmoon.lights.MoonLampLineLightBehavior;
import com.sighs.handheldmoon.lights.PlayerFlashlightLineLightBehavior;
import com.sighs.handheldmoon.registry.Config;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import dev.lambdaurora.lambdynlights.engine.DynamicLightingEngine;
import dev.lambdaurora.lambdynlights.engine.lookup.SpatialLookupDeferredEntry;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpatialLookupDeferredEntry.class, remap = false)
public class SpatialLookupDeferredEntryMixin {

    @Shadow
    @Final
    private DynamicLightBehavior behavior;

    @Inject(method = "getDynamicLightLevel", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$scaleFalloff(BlockPos pos, CallbackInfoReturnable<Double> cir) {
        if (behavior instanceof PlayerFlashlightLineLightBehavior || behavior instanceof MoonLampLineLightBehavior) {
            double scaled = (15.0 / DynamicLightingEngine.MAX_RADIUS) * Config.engineFalloffScale();
            double luminance = this.behavior.lightAtPos(pos, scaled);
            cir.setReturnValue(Math.max(luminance, 0.0));
        }
    }
}