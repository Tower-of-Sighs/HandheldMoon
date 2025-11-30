package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.init.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.sodiumdynamiclights.DynamicLightSource;
import toni.sodiumdynamiclights.SodiumDynamicLights;

@Mixin(value = SodiumDynamicLights.class, remap = false)
public abstract class DirectionalLightMixin {
    @Inject(method = "maxDynamicLightLevel", at = @At("HEAD"), cancellable = true)
    private static void onMaxDynamicLightLevel(BlockPos pos, DynamicLightSource lightSource,
                                               double currentLightLevel, CallbackInfoReturnable<Double> cir) {
        if (lightSource instanceof Player player) {
            if (!Utils.isUsingFlashlight(player)) return;

            // 获取玩家视线方向
            Vec3 lookVec = player.getLookAngle();

            // 计算从玩家眼睛到目标点的方向
            Vec3 toTarget = new Vec3(
                    pos.getX() - lightSource.sdl$getDynamicLightX() + 0.5,
                    pos.getY() - lightSource.sdl$getDynamicLightY() + 0.5,
                    pos.getZ() - lightSource.sdl$getDynamicLightZ() + 0.5
            ).normalize();

            // 计算夹角的余弦值
            double dotProduct = lookVec.dot(toTarget);
            double angle = Math.acos(dotProduct);

            // 定义锥形区域的角度阈值（单位：弧度）
            double innerAngle = 0.4;  // 完全亮度的内锥角（约23度）
            double outerAngle = 0.6;  // 光照衰减到零的外锥角（约34度）

            // 计算角度衰减系数
            double angleAttenuation = calculateAngleAttenuation(angle, innerAngle, outerAngle);

            // 如果衰减系数为0，直接返回0光照
            if (angleAttenuation <= 0.0) {
                cir.setReturnValue(0.0);
                return;
            }

            int luminance = lightSource.sdl$getLuminance();
            if (luminance > 0) {
                double dx = (double)pos.getX() - lightSource.sdl$getDynamicLightX() + 0.5;
                double dy = (double)pos.getY() - lightSource.sdl$getDynamicLightY() + 0.5;
                double dz = (double)pos.getZ() - lightSource.sdl$getDynamicLightZ() + 0.5;
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                if (distanceSquared <= 1024.0) {  // 32格距离限制
                    double distance = Math.sqrt(distanceSquared);
                    double distanceMultiplier = 1.0 - distance / 32.0;  // 距离衰减

                    // 计算最终光照强度：基础光照 × 距离衰减 × 角度衰减
                    double lightLevel = distanceMultiplier * luminance * angleAttenuation;

                    if (lightLevel > currentLightLevel) {
                        cir.setReturnValue(lightLevel);
                    }
                }
            }
        }
    }

    /**
     * 计算角度衰减系数
     * @param currentAngle 当前角度（弧度）
     * @param innerAngle 内锥角，完全亮度的边界
     * @param outerAngle 外锥角，光照衰减到零的边界
     * @return 衰减系数（0.0到1.0）
     */
    private static double calculateAngleAttenuation(double currentAngle, double innerAngle, double outerAngle) {
        if (currentAngle <= innerAngle) {
            // 在内锥角内，完全亮度
            return 1.0;
        } else if (currentAngle >= outerAngle) {
            // 在外锥角外，无光照
            return 0.0;
        } else {
            // 在过渡区域内，线性衰减
            double transitionRange = outerAngle - innerAngle;
            double positionInTransition = (currentAngle - innerAngle) / transitionRange;
            return 1.0 - positionInTransition;  // 线性衰减
        }
    }

//    @Inject(method = "getLivingEntityLuminanceFromItems", at = @At("HEAD"), cancellable = true)
//    private static void enforce(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
//        if (entity instanceof Player player) {
//            if (Utils.isUsingFlashlight(player)) cir.setReturnValue(15);
//        }
//    }

    @Inject(method = "getLuminanceFromItemStack", at = @At("HEAD"), cancellable = true)
    private static void enforce(ItemStack stack, boolean submergedInWater, CallbackInfoReturnable<Integer> cir) {
        if (Utils.isFlashlight(stack)) cir.setReturnValue(15);
    }
}
