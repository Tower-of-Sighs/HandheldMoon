package com.sighs.handheldmoon.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

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
                                      double cosInner,
                                      double cosOuter) {
        double cx = query.getX() + 0.5;
        double cy = query.getY() + 0.5;
        double cz = query.getZ() + 0.5;
        double dx0 = cx - sx;
        double dy0 = cy - sy;
        double dz0 = cz - sz;
        double dist2 = dx0 * dx0 + dy0 * dy0 + dz0 * dz0;
        double range2 = range * range;
        if (dist2 > range2) return 0.0;
        double dot = dx * dx0 + dy * dy0 + dz * dz0;
        if (dot <= 0.0) return 0.0;
        double cosOuterSq = cosOuter * cosOuter;
        if (dot * dot < cosOuterSq * dist2) return 0.0;
        double invDistF = Mth.fastInvSqrt(dist2);
        double dotNorm = dot * invDistF;
        double angleAtt = dotNorm >= cosInner ? 1.0 : (dotNorm - cosOuter) / (cosInner - cosOuter);
        double dist = 1.0 / invDistF;
        double distMul = 1.0 - (dist / range);
        double res = luminance * angleAtt * distMul;
        return Math.max(res, 0.0);
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
}
