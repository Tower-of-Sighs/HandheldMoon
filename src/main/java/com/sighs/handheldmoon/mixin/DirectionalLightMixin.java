package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

@Mixin(value = SodiumDynamicLights.class, remap = false)
public abstract class DirectionalLightMixin {
    @Inject(method = "maxDynamicLightLevel", at = @At("HEAD"), cancellable = true)
    private static void onMaxDynamicLightLevel(BlockPos pos, DynamicLightSource lightSource,
                                               double currentLightLevel, CallbackInfoReturnable<Double> cir) {
        if (lightSource instanceof Player player) {
            if (!Config.REAL_LIGHT.get()) return;
            if (!Utils.isUsingFlashlight(player)) return;

            Vec3 lookVec = player.getLookAngle();

            // 方向
            Vec3 toTarget = new Vec3(
                    pos.getX() - lightSource.sdl$getDynamicLightX() + 0.5,
                    pos.getY() - lightSource.sdl$getDynamicLightY() + 0.5,
                    pos.getZ() - lightSource.sdl$getDynamicLightZ() + 0.5
            ).normalize();

            // 夹角的余弦值
            double dotProduct = lookVec.dot(toTarget);
            double angle = Math.acos(dotProduct);

            // 锥形区域的角度
            double innerAngle = 0.5;
            double outerAngle = 0.7;

            // 衰减
            double angleAttenuation = calculateAngleAttenuation(angle, innerAngle, outerAngle);

            int luminance = lightSource.sdl$getLuminance();

            if (angleAttenuation <= 0.0) {
                cir.setReturnValue(currentLightLevel);
            } else {
                if (luminance > 0) {
                    double dx = (double) pos.getX() - lightSource.sdl$getDynamicLightX() + 0.5;
                    double dy = (double) pos.getY() - lightSource.sdl$getDynamicLightY() + 0.5;
                    double dz = (double) pos.getZ() - lightSource.sdl$getDynamicLightZ() + 0.5;
                    double distanceSquared = dx * dx + dy * dy + dz * dz;

                    if (distanceSquared <= 1024.0) {
                        double distance = Math.sqrt(distanceSquared);
                        double distanceMultiplier = 1.0 - distance / 32.0;

                        // 计算最终光照强度：基础光照 × 距离衰减 × 角度衰减
                        double lightLevel = distanceMultiplier * luminance * angleAttenuation;

                        if (lightLevel > currentLightLevel) {
                            cir.setReturnValue(Math.max(lightLevel + 0.5, currentLightLevel));
                        }
                    }
                }
            }
        }
    }

    // 计算角度衰减系数
    private static double calculateAngleAttenuation(double currentAngle, double innerAngle, double outerAngle) {
        if (currentAngle <= innerAngle) {
            return 1.0;
        } else if (currentAngle >= outerAngle) {
            return 0.0;
        } else {
            double transitionRange = outerAngle - innerAngle;
            double positionInTransition = (currentAngle - innerAngle) / transitionRange;
            return 1.0 - positionInTransition;
        }
    }

    @Inject(method = "getLuminanceFromItemStack", at = @At("HEAD"), cancellable = true)
    private static void enforce(ItemStack stack, boolean submergedInWater, CallbackInfoReturnable<Integer> cir) {
        if (Utils.isFlashlight(stack)) cir.setReturnValue(15);
    }

    @Inject(method = "updateTracking", at = @At("HEAD"), cancellable = true)
    private static void enforce(DynamicLightSource lightSource, CallbackInfo ci) {
        if (lightSource instanceof MoonlightLampBlockEntity) {
            lightSource.sdl$setDynamicLightEnabled(true);
            ci.cancel();
        }
    }
}
