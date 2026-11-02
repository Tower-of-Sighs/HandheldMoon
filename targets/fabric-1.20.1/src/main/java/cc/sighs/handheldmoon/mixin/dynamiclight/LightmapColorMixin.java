package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.LightmapColorBridge;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the generated 1.20.1 lightmap texture into the common coefficient
 * table, so block/entity tinting can preserve the vanilla ambient background.
 */
@Mixin(LightTexture.class)
public abstract class LightmapColorMixin {
    @Shadow
    @Final
    private NativeImage lightPixels;

    @Inject(method = "updateLightTexture", at = @At("TAIL"))
    private void handheldmoon$publishLightmap(float partialTick, CallbackInfo callback) {
        if (this.lightPixels == null) {
            return;
        }
        double[] table = new double[16 * 16 * 3];
        for (int sky = 0; sky < 16; sky++) {
            for (int block = 0; block < 16; block++) {
                int pixel = this.lightPixels.getPixelRGBA(block, sky);
                int offset = (sky * 16 + block) * 3;
                table[offset] = (pixel & 0xFF) / 255.0;
                table[offset + 1] = ((pixel >> 8) & 0xFF) / 255.0;
                table[offset + 2] = ((pixel >> 16) & 0xFF) / 255.0;
            }
        }
        LightmapColorBridge.publishIfChanged(table);
    }
}
