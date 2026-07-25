package cc.sighs.handheldmoon.util;

import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
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
        double dist = 1.0 / invDistF;
        double dotNorm = dot * invDistF;
        double angleAtt = dotNorm >= cosInner ? 1.0 : (dotNorm - cosOuter) / (cosInner - cosOuter);
        double distMul = 1.0 - smoothstep01(dist / range);
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
        Vec3 end = new Vec3(query.getX() + 0.5, query.getY() + 0.5, query.getZ() + 0.5);
        if (!isRayVisible(level, sx, sy, sz, end, query, 0)) return 0.0;
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
        if (distSq < 1.0e-8) return luminance;
        double invDist = Mth.fastInvSqrt((float) distSq);
        double dist = 1.0 / invDist;
        double distanceMultiplier = 1.0 - smoothstep01(dist / range);
        double res = luminance * distanceMultiplier;
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
        SharedLightMath.Direction direction = SharedLightMath.direction(yawDeg, pitchDeg, lampMode);
        return new Vec3(direction.x(), direction.y(), direction.z());
    }

    public static double effectiveRange(double luminance, double range, double threshold) {
        return SharedLightMath.effectiveRange(luminance, range, threshold);
    }

    public static double conePadding(double distance, double outerAngleRad, double minPad, double maxPad) {
        return SharedLightMath.conePadding(distance, outerAngleRad, minPad, maxPad);
    }

    public static long getBlockVolume(DynamicLightBehavior.BoundingBox box) {
        return SharedLightMath.volume(box.startX(), box.startY(), box.startZ(), box.endX(), box.endY(), box.endZ());
    }

    private static double smoothstep01(double x) {
        double t = Mth.clamp(x, 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }
}
