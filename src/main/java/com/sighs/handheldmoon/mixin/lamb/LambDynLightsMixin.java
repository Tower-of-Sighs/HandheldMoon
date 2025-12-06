package com.sighs.handheldmoon.mixin.lamb;

import com.sighs.handheldmoon.compat.tacz.TaczLambDynLightsCompat;
import dev.lambdaurora.lambdynlights.LambDynLights;
import dev.lambdaurora.lambdynlights.compat.CompatLayer;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LambDynLights.class)
public class LambDynLightsMixin {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void injectTaczCompat(CallbackInfo ci) {
        if (FabricLoader.getInstance().isModLoaded("tacz")) {
            CompatLayer.LAYERS.add(new TaczLambDynLightsCompat());
        }
    }
}
