package com.sighs.handheldmoon.util;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;

public final class LineLightMath {

    private LineLightMath() {
    }

    /**
     * 计算 query 与光源方向关系，并返回亮度（未考虑是否 powered）
     */
    public static double computeLight(double sx, double sy, double sz,
                                      double dx, double dy, double dz,
                                      double luminance,
                                      BlockPos query,
                                      double range,
                                      double innerAngleRad,
                                      double outerAngleRad) {
        double cx = query.getX() + 0.5;
        double cy = query.getY() + 0.5;
        double cz = query.getZ() + 0.5;
        double vx = cx - sx;
        double vy = cy - sy;
        double vz = cz - sz;
        double dist2 = vx * vx + vy * vy + vz * vz;
        double range2 = range * range;
        if (dist2 > range2) return 0.0;
        double dot = dx * vx + dy * vy + dz * vz;
        if (dot <= 0.0) return 0.0;
        double cosInner = Math.cos(innerAngleRad);
        double cosOuter = Math.cos(outerAngleRad);
        double cosOuterSq = cosOuter * cosOuter;
        if (dot * dot < cosOuterSq * dist2) return 0.0;
        double invDistF = Mth.fastInvSqrt((float) dist2);
        double dotNorm = dot * invDistF;
        double angleAtt = dotNorm >= cosInner ? 1.0 : (dotNorm - cosOuter) / (cosInner - cosOuter);
        double distMul = 1.0 - (dist2 / range2);
        double res = luminance * angleAtt * distMul;
        return Math.max(res, 0.0);
    }

    public static double computeLightOccluded(Level level,
                                              double sx, double sy, double sz,
                                              double dx, double dy, double dz,
                                              double luminance,
                                              BlockPos query,
                                              double range,
                                              double innerAngleRad,
                                              double outerAngleRad) {
        double res = computeLight(sx, sy, sz, dx, dy, dz, luminance, query, range, innerAngleRad, outerAngleRad);
        if (res <= 0.0) return 0.0;
        if (level == null) return res;
        Vec3 start = new Vec3(sx, sy, sz);
        Vec3 end = new Vec3(query.getX() + 0.5, query.getY() + 0.5, query.getZ() + 0.5);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(query)) return 0.0;
        return res;
    }

    public static double computePointLightOccluded(Level level,
                                                   double sx, double sy, double sz,
                                                   double luminance,
                                                   BlockPos query,
                                                   double range) {
        double cx = query.getX() + 0.5;
        double cy = query.getY() + 0.5;
        double cz = query.getZ() + 0.5;
        double dx = cx - sx;
        double dy = cy - sy;
        double dz = cz - sz;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq > rangeSq) return 0.0;
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double t3 = (distSq * dist) / (range * range * range);
        double distanceMultiplier = 1.0 - t3;
        double res = luminance * distanceMultiplier;
        if (res <= 0.0) return 0.0;
        if (level == null) return res;
        Vec3 start = new Vec3(sx, sy, sz);
        Vec3 endCenter = new Vec3(cx, cy, cz);
        Vec3 endUp = new Vec3(cx, cy + 0.25, cz);
        Vec3 endDown = new Vec3(cx, cy - 0.25, cz);
        var hitCenter = level.clip(new ClipContext(start, endCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        boolean passCenter = !(hitCenter.getType() == HitResult.Type.BLOCK && !hitCenter.getBlockPos().equals(query));
        if (passCenter) return res;
        var hitUp = level.clip(new ClipContext(start, endUp, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        boolean passUp = !(hitUp.getType() == HitResult.Type.BLOCK && !hitUp.getBlockPos().equals(query));
        var hitDown = level.clip(new ClipContext(start, endDown, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        boolean passDown = !(hitDown.getType() == HitResult.Type.BLOCK && !hitDown.getBlockPos().equals(query));
        if (passUp || passDown) return res * 0.6;
        return 0.0;
    }

    /**
     * 根据 yaw/pitch 计算方向向量
     */
    public static Vec3 computeDirection(float yawDeg, float pitchDeg, boolean lampMode) {

        double yaw = yawDeg * Mth.DEG_TO_RAD;
        double pitch = pitchDeg * Mth.DEG_TO_RAD;

        if (lampMode) {
            return new Vec3(
                    Math.sin(yaw) * Math.cos(pitch),
                    -Math.sin(pitch),
                    Math.cos(yaw) * Math.cos(pitch)
            );
        } else {
            return new Vec3(
                    -Math.sin(yaw) * Math.cos(pitch),
                    -Math.sin(pitch),
                    Math.cos(yaw) * Math.cos(pitch)
            );
        }
    }

    public static long getBlockVolume(DynamicLightBehavior.BoundingBox box) {
        long dx = (long) box.endX() - box.startX() + 1;
        long dy = (long) box.endY() - box.startY() + 1;
        long dz = (long) box.endZ() - box.startZ() + 1;
        return dx * dy * dz;
    }
}
