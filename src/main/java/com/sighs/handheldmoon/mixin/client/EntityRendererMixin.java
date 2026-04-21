package com.sighs.handheldmoon.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sighs.handheldmoon.util.Utils;
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

//    @WrapOperation(
//            method = "getPackedLightCoords",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getSkyLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I"
//            )
//    )
//    private int handheldMoon$boostSkyWhenFlashlight(EntityRenderer<?> instance, Entity entity, BlockPos pos, Operation<Integer> original) {
//        int sky = original.call(instance, entity, pos);
//        boolean indoor = sky <= 0;
//        if (entity instanceof Player player) {
//            if (Utils.isUsingFlashlight(player) && indoor) {
//                return 12;
//            }
//        }
//        var mc = Minecraft.getInstance();
//        if (mc.level != null && !(entity instanceof Player && Utils.isUsingFlashlight((Player) entity))) {
//            for (Player other : mc.level.players()) {
//                if (other == entity) continue;
//                if (Utils.isUsingFlashlight(other)) {
//                    double dist2 = other.distanceToSqr(entity);
//                    if (dist2 < 144.0 && indoor) {
//                        return 10;
//                    }
//                }
//            }
//        }
//        return sky;
//    }
}
