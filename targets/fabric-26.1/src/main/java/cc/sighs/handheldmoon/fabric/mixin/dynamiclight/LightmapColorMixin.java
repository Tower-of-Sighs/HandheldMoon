package cc.sighs.handheldmoon.fabric.mixin.dynamiclight;
import cc.sighs.handheldmoon.dynamiclight.LightmapColorBridge;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Lightmap.class)
public abstract class LightmapColorMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void handheldmoon$captureLightmap(LightmapRenderState state, CallbackInfo ci) {
        if (!state.needsUpdate) return;
        LightmapColorBridge.update(new LightmapColorBridge.Parameters(
                state.blockFactor, state.skyFactor, state.ambientColor, state.nightVisionColor,
                state.nightVisionEffectIntensity, state.skyLightColor, state.blockLightTint,
                state.bossOverlayWorldDarkening, state.darknessEffectScale, state.brightness));
    }
}
