package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public final class LineLightMath {
    private static final CollisionContext EMPTY_COLLISION = CollisionContext.empty();

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
        return computeLight(sx, sy, sz, dx, dy, dz, luminance, query, range,
                innerAngleRad, outerAngleRad, AttenuationCurve.QUADRATIC);
    }

    public static double computeLight(double sx, double sy, double sz,
                                      double dx, double dy, double dz,
                                      double luminance,
                                      BlockPos query,
                                      double range,
                                      double innerAngleRad,
                                      double outerAngleRad,
                                      AttenuationCurve curve) {
        double cosInner = Mth.cos((float) innerAngleRad);
        double cosOuter = Mth.cos((float) outerAngleRad);
        return computeLightWithCos(
                sx, sy, sz, dx, dy, dz, luminance,
                query.getX(), query.getY(), query.getZ(), range,
                cosInner, cosOuter, cosOuter * cosOuter, curve
        );
    }

    /** Same cone calculation with precomputed angular constants for hot query loops. */
    public static double computeLightWithCos(double sx, double sy, double sz,
                                             double dx, double dy, double dz,
                                             double luminance,
                                             int blockX, int blockY, int blockZ,
                                             double range,
                                             double cosInner,
                                             double cosOuter,
                                             double cosOuterSq) {
        return computeLightWithCos(sx, sy, sz, dx, dy, dz, luminance,
                blockX, blockY, blockZ, range, cosInner, cosOuter, cosOuterSq,
                AttenuationCurve.QUADRATIC);
    }

    public static double computeLightWithCos(double sx, double sy, double sz,
                                             double dx, double dy, double dz,
                                             double luminance,
                                             int blockX, int blockY, int blockZ,
                                             double range,
                                             double cosInner,
                                             double cosOuter,
                                             double cosOuterSq,
                                             AttenuationCurve curve) {
        double cx = blockX + 0.5;
        double cy = blockY + 0.5;
        double cz = blockZ + 0.5;
        double vx = cx - sx;
        double vy = cy - sy;
        double vz = cz - sz;
        double dist2 = vx * vx + vy * vy + vz * vz;
        double range2 = range * range;
        if (dist2 >= range2) return 0.0;
        if (dist2 < 1.0E-12) return Math.max(luminance, 0.0);
        double dot = dx * vx + dy * vy + dz * vz;
        if (dot <= 0.0) return 0.0;
        if (dot * dot < cosOuterSq * dist2) return 0.0;
        double invDistF = Mth.fastInvSqrt((float) dist2);
        double dist = 1.0 / invDistF;
        double dotNorm = dot * invDistF;
        double angleAtt = Math.abs(cosInner - cosOuter) < 1.0E-9
                ? (dotNorm >= cosOuter ? 1.0 : 0.0)
                : (dotNorm >= cosInner ? 1.0 : (dotNorm - cosOuter) / (cosInner - cosOuter));
        double res = SharedLightMath.attenuatedIntensity(luminance, dist, range, curve) * angleAtt;
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
        return computeLightOccluded(level, sx, sy, sz, dx, dy, dz, luminance, query,
                range, innerAngleRad, outerAngleRad, AttenuationCurve.QUADRATIC);
    }

    public static double computeLightOccluded(Level level,
                                              double sx, double sy, double sz,
                                              double dx, double dy, double dz,
                                              double luminance,
                                              BlockPos query,
                                              double range,
                                              double innerAngleRad,
                                              double outerAngleRad,
                                              AttenuationCurve curve) {
        double res = computeLight(sx, sy, sz, dx, dy, dz, luminance, query, range,
                innerAngleRad, outerAngleRad, curve);
        if (res <= 0.0) return 0.0;
        if (level == null) return res;
        Vec3 end = new Vec3(query.getX() + 0.5, query.getY() + 0.5, query.getZ() + 0.5);
        if (!isRayVisible(level, sx, sy, sz, end, query, 0)) return 0.0;
        return res;
    }

    public static double computePointLightOccluded(Level level,
                                                   double sx, double sy, double sz,
                                                   double luminance,
                                                   BlockPos query,
                                                   double range) {
        return computePointLightOccluded(level, sx, sy, sz, luminance, query, range,
                AttenuationCurve.QUADRATIC);
    }

    public static double computePointLightOccluded(Level level,
                                                   double sx, double sy, double sz,
                                                   double luminance,
                                                   BlockPos query,
                                                   double range,
                                                   AttenuationCurve curve) {
        double cx = query.getX() + 0.5;
        double cy = query.getY() + 0.5;
        double cz = query.getZ() + 0.5;
        double dx = cx - sx;
        double dy = cy - sy;
        double dz = cz - sz;
        double distSq = dx * dx + dy * dy + dz * dz;
        double rangeSq = range * range;
        if (distSq >= rangeSq) return 0.0;
        if (distSq < 1.0e-8) return Math.max(luminance, 0.0);
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double res = SharedLightMath.attenuatedIntensity(luminance, dist, range, curve);
        if (res <= 0.0) return 0.0;
        if (level == null) return res;
        Vec3 endCenter = new Vec3(cx, cy, cz);
        boolean passCenter = isRayVisible(level, sx, sy, sz, endCenter, query, 0);
        return passCenter ? res : 0.0;
    }

    public static int chooseOcclusionBucketShift(double lightLevel) {
        // Keep only 1 and 2 block bucket sizes for quality; avoid 4-block buckets that cause visible stair-steps.
        if (lightLevel >= 7.0) {
            return 0;
        }
        return 1;
    }

    public static long occlusionBucketKey(BlockPos pos, int shift) {
        int x = pos.getX() >> shift;
        int y = pos.getY() >> shift;
        int z = pos.getZ() >> shift;
        return (BlockPos.asLong(x, y, z) << 2) | (shift & 0x3L);
    }

    public static Vec3 occlusionBucketCenter(BlockPos pos, int shift) {
        int size = 1 << shift;
        double half = size * 0.5;
        int baseX = (pos.getX() >> shift) << shift;
        int baseY = (pos.getY() >> shift) << shift;
        int baseZ = (pos.getZ() >> shift) << shift;
        return new Vec3(baseX + half, baseY + half, baseZ + half);
    }

    public static boolean isRayVisible(Level level,
                                       double sx, double sy, double sz,
                                       Vec3 end,
                                       BlockPos query,
                                       int bucketShift) {
        if (level == null) {
            return true;
        }

        Vec3 start = new Vec3(sx, sy, sz);
        var hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, EMPTY_COLLISION));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return true;
        }

        BlockPos hitPos = hit.getBlockPos();
        BlockPos sourcePos = BlockPos.containing(sx, sy, sz);
        if (hitPos.equals(sourcePos)) {
            return true;
        }

        if (bucketShift <= 0) {
            return hitPos.equals(query);
        }

        return inSameOcclusionBucket(hitPos, query, bucketShift);
    }

    public static boolean inSameOcclusionBucket(BlockPos a, BlockPos b, int shift) {
        return (a.getX() >> shift) == (b.getX() >> shift)
                && (a.getY() >> shift) == (b.getY() >> shift)
                && (a.getZ() >> shift) == (b.getZ() >> shift);
    }

    public static double preciseVisibilityFactor(Level level,
                                                 double sx, double sy, double sz,
                                                 BlockPos query) {
        if (level == null) {
            return 1.0;
        }

        double cx = query.getX() + 0.5;
        double cy = query.getY() + 0.5;
        double cz = query.getZ() + 0.5;

        int visibleCount = 0;
        if (isRayVisible(level, sx, sy, sz, new Vec3(cx, cy, cz), query, 0)) {
            visibleCount++;
        }
        if (isRayVisible(level, sx, sy, sz, new Vec3(cx, cy + 0.28, cz), query, 0)) {
            visibleCount++;
        }
        if (isRayVisible(level, sx, sy, sz, new Vec3(cx, cy - 0.28, cz), query, 0)) {
            visibleCount++;
        }

        return visibleCount / 3.0;
    }

    /**
     * 根据 yaw/pitch 计算方向向量
     */
    public static Vec3 computeDirection(float yawDeg, float pitchDeg, boolean lampMode) {
        float yaw = yawDeg * Mth.DEG_TO_RAD;
        float pitch = pitchDeg * Mth.DEG_TO_RAD;
        SharedLightMath.Direction direction = SharedLightMath.direction(
                Mth.sin(yaw), Mth.cos(yaw), Mth.sin(pitch), Mth.cos(pitch), lampMode
        );
        return new Vec3(direction.x(), direction.y(), direction.z());
    }

    public static double effectiveRange(double luminance, double range, double threshold) {
        return SharedLightMath.effectiveRange(luminance, range, threshold);
    }

    public static double effectiveRange(double luminance, double range, double threshold,
                                        AttenuationCurve curve) {
        return SharedLightMath.effectiveRange(luminance, range, threshold, curve);
    }

    public static double conePadding(double distance, double outerAngleRad, double minPad, double maxPad) {
        float angle = (float) outerAngleRad;
        double tangent = Mth.sin(angle) / Mth.cos(angle);
        return SharedLightMath.conePadding(distance, tangent, minPad, maxPad);
    }

    public static long getBlockVolume(DynamicLightBehavior.Bounds box) {
        return SharedLightMath.volume(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

}
