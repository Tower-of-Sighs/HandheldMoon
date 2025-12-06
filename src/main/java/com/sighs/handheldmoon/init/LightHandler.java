package com.sighs.handheldmoon.init;

import com.sighs.handheldmoon.block.FullMoonBlockEntity;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.compat.TaczCompat;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.event.Cache;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import toni.sodiumdynamiclights.DynamicLightSource;

public class LightHandler {
    public static void entityLight(BlockPos pos, DynamicLightSource lightSource, double currentLightLevel, CallbackInfoReturnable<Double> cir) {
        if (!Cache.getRealLightSourceList().contains(lightSource)) return;
        Vec3 lookVec = Vec3.ZERO;
        double radius = 32;

        if (lightSource instanceof Player player) {
            lookVec = player.getLookAngle();
        }

        boolean isFullMoonBlock = false;
        if (lightSource instanceof FullMoonEntity entity) {
            var be = entity.level().getBlockEntity(entity.blockPosition());
            if (be instanceof MoonlightLampBlockEntity lamp) {
                lookVec = lamp.getViewVec();
            } else if (be instanceof FullMoonBlockEntity) {
                isFullMoonBlock = true;
                radius = 18;
            }
        }

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
        if (lookVec.equals(Vec3.ZERO)) angleAttenuation = 1;

        int luminance = lightSource.sdl$getLuminance();

        if (angleAttenuation <= 0.0) {
            cir.setReturnValue(currentLightLevel);
        } else {
            if (luminance > 0) {
                double dx = (double) pos.getX() - lightSource.sdl$getDynamicLightX() + 0.5;
                double dy = (double) pos.getY() - lightSource.sdl$getDynamicLightY() + 0.5;
                double dz = (double) pos.getZ() - lightSource.sdl$getDynamicLightZ() + 0.5;
                double distanceSquared = dx * dx + dy * dy + dz * dz;

                if (distanceSquared <= radius * radius) {
                    double distance = Math.sqrt(distanceSquared);
                    double distanceMultiplier;
                    double t = distance / radius; // 标准化距离，t 在 [0,1] 之间
                    if (isFullMoonBlock) {
                        distanceMultiplier = 1.0 - t * t * t;
                    } else {
                        distanceMultiplier = 1.0 - t * t;
                    }

                    // 计算最终光照强度：基础光照 × 距离衰减 × 角度衰减
                    double lightLevel = distanceMultiplier * luminance * angleAttenuation;

                    if (lightLevel > currentLightLevel) {
                        if (lightSource instanceof Player player) {
                            if (!TaczCompat.isUsingAttachmentFlashlight(player)) {
                            }
                        }
                        Level level = (lightSource instanceof Player p) ? p.level() : (lightSource instanceof FullMoonEntity e) ? e.level() : null;
                        if (level != null && Config.LIGHT_OCCLUSION.get()) {
                            Vec3 start = new Vec3(lightSource.sdl$getDynamicLightX() + 0.5, lightSource.sdl$getDynamicLightY() + 0.5, lightSource.sdl$getDynamicLightZ() + 0.5);
                            Vec3 center = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                            if (isFullMoonBlock) {
                                var hitCenter = level.clip(new ClipContext(start, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                                boolean passCenter = !(hitCenter.getType() == HitResult.Type.BLOCK && !hitCenter.getBlockPos().equals(pos));
                                if (!passCenter) {
                                    Vec3 up = new Vec3(center.x, center.y + 0.25, center.z);
                                    Vec3 down = new Vec3(center.x, center.y - 0.25, center.z);
                                    var hitUp = level.clip(new ClipContext(start, up, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                                    boolean passUp = !(hitUp.getType() == HitResult.Type.BLOCK && !hitUp.getBlockPos().equals(pos));
                                    var hitDown = level.clip(new ClipContext(start, down, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                                    boolean passDown = !(hitDown.getType() == HitResult.Type.BLOCK && !hitDown.getBlockPos().equals(pos));
                                    if (passUp || passDown) {
                                        lightLevel *= 0.6;
                                    } else {
                                        lightLevel = currentLightLevel;
                                    }
                                }
                            } else {
                                var hit = level.clip(new ClipContext(start, center, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                                if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(pos)) {
                                    lightLevel = currentLightLevel;
                                }
                            }
                        }
                        if (lightLevel > currentLightLevel) {
                            cir.setReturnValue(Math.max(lightLevel + 0.3, currentLightLevel));
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

    public static void selfLight(LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (Cache.getSelfLightSourceList().contains((DynamicLightSource) entity)) cir.setReturnValue(15);
    }
}
