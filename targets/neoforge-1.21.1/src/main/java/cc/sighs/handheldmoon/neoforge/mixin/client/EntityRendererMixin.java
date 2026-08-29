package cc.sighs.handheldmoon.neoforge.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @ModifyReturnValue(
            method = "getPackedLightCoords",
            at = @At("RETURN")
    )
    private int handheldMoon$forceBlockFullBrightIfUsingFlashlight(int original, Entity entity, float partialTicks) {
        if (entity instanceof Player player && Utils.isUsingFlashlight(player)) {
            return LightTexture.pack(15, LightTexture.sky(original));
        }
        return original;
    }
}
