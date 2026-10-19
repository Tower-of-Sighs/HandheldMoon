package cc.sighs.handheldmoon.fabric.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve dynamic-light invalidations until Sodium can accept them. */
@Pseudo
@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class SodiumSectionReadinessMixin {
    @Shadow private RenderSectionManager renderSectionManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void handheldmoon$registerReadiness(CallbackInfo ci) {
        DynamicLightManager.setSectionReadyCheck((x, y, z) ->
                renderSectionManager != null && renderSectionManager.isSectionBuilt(x, y, z));
    }
}
