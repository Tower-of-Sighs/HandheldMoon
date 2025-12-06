package com.sighs.handheldmoon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @WrapOperation(
            method = "getPackedLightCoords",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I"
            )
    )
    private int handheldMoon$onForceEntityLitUp(EntityRenderer<?> instance, Entity entity, BlockPos pos, Operation<Integer> original) {
        if (entity instanceof Player player) {
            if (Utils.isUsingFlashlight(player)) {
                return 15;
            }
        }
        return original.call(instance, entity, pos);
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
