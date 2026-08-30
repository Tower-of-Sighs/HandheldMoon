package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;

/** Pure light geometry shared by Minecraft-version adapters. */
public final class SharedLightMath {
    private SharedLightMath() {
    }

    public static Direction direction(double sinYaw, double cosYaw,
                                      double sinPitch, double cosPitch,
                                      boolean lampMode) {
        double x = sinYaw * cosPitch;
        if (!lampMode) {
            x = -x;
        }
        return new Direction(x, -sinPitch, cosYaw * cosPitch);
    }

    public static double effectiveRange(double luminance, double range, double threshold) {
        return effectiveRange(luminance, range, threshold, AttenuationCurve.QUADRATIC);
    }

    /**
     * Conservative radius at which a curve falls below a rebuild threshold.
     * A short binary search keeps this independent of the curve formula and
     * always returns a value inside the configured hard radius.
     */
    public static double effectiveRange(double luminance, double range, double threshold,
                                        AttenuationCurve curve) {
        if (luminance <= threshold) {
            return 0.0;
        }
        if (range <= 0.0) {
            return 0.0;
        }
        if (curve == AttenuationCurve.NONE) {
            return range;
        }
        if (curve == null || curve == AttenuationCurve.QUADRATIC) {
            double factor = 1.0 - threshold / luminance;
            return range * Math.sqrt(Math.max(0.0, factor));
        }
        double target = Math.max(0.0, Math.min(1.0, threshold / luminance));
        double low = 0.0;
        double high = 1.0;
        // Curves are monotonic decreasing; find the last point at/above target.
        for (int i = 0; i < 32; i++) {
            double mid = (low + high) * 0.5;
            if (curve.at(mid) >= target) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return range * low;
    }

    /** Default quadratic distance attenuation retained for old callers. */
    public static double distanceAttenuation(double distance, double range) {
        return distanceAttenuation(distance, range, AttenuationCurve.QUADRATIC);
    }

    /**
     * Evaluates a normalized distance falloff. The radius is a hard boundary:
     * values at or beyond it are always zero, independent of curve choice.
     */
    public static double distanceAttenuation(double distance, double range, AttenuationCurve curve) {
        if (range <= 0.0 || distance >= range) {
            return 0.0;
        }
        if (distance <= 0.0 || !Double.isFinite(distance)) {
            return distance <= 0.0 ? 1.0 : 0.0;
        }
        if (curve == null) {
            curve = AttenuationCurve.QUADRATIC;
        }
        double t = distance / range;
        if (!Double.isFinite(t) || t >= 1.0) {
            return 0.0;
        }
        if (t <= 0.0) {
            return 1.0;
        }
        return curve.at(t);
    }

    /**
     * Converts peak point intensity into the final distance-attenuated value.
     * Both peak intensity and radius are inputs by design; the hard-radius
     * rule is enforced by {@link #distanceAttenuation(double, double, AttenuationCurve)}.
     */
    public static double attenuatedIntensity(double peakIntensity, double distance,
                                             double radius, AttenuationCurve curve) {
        return Math.max(peakIntensity, 0.0) * distanceAttenuation(distance, radius, curve);
    }

    /** Pure-Java cone sample used by background section batch workers. */
    public static double coneLight(
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double luminance,
            int blockX, int blockY, int blockZ,
            double range,
            double cosInner, double cosOuter, double cosOuterSq
    ) {
        return coneLight(originX, originY, originZ, directionX, directionY, directionZ,
                luminance, blockX, blockY, blockZ, range, cosInner, cosOuter, cosOuterSq,
                AttenuationCurve.QUADRATIC);
    }

    public static double coneLight(
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double luminance,
            int blockX, int blockY, int blockZ,
            double range,
            double cosInner, double cosOuter, double cosOuterSq,
            AttenuationCurve curve
    ) {
        double centerX = blockX + 0.5;
        double centerY = blockY + 0.5;
        double centerZ = blockZ + 0.5;
        double vectorX = centerX - originX;
        double vectorY = centerY - originY;
        double vectorZ = centerZ - originZ;
        double distanceSquared = vectorX * vectorX + vectorY * vectorY + vectorZ * vectorZ;
        if (distanceSquared >= range * range) {
            return 0.0;
        }
        if (distanceSquared < 1.0E-12) {
            return Math.max(luminance, 0.0);
        }
        double dot = directionX * vectorX + directionY * vectorY + directionZ * vectorZ;
        if (dot <= 0.0 || dot * dot < cosOuterSq * distanceSquared) {
            return 0.0;
        }
        double inverseDistance = 1.0 / Math.sqrt(distanceSquared);
        double dotNormal = dot * inverseDistance;
        double angleAttenuation = Math.abs(cosInner - cosOuter) < 1.0E-9
                ? (dotNormal >= cosOuter ? 1.0 : 0.0)
                : (dotNormal >= cosInner
                ? 1.0
                : (dotNormal - cosOuter) / (cosInner - cosOuter));
        return Math.max(attenuatedIntensity(luminance, 1.0 / inverseDistance, range, curve)
                * angleAttenuation, 0.0);
    }

    /** Pure-Java point sample used by background section batch workers. */
    public static double pointLight(
            double originX, double originY, double originZ,
            double luminance,
            int blockX, int blockY, int blockZ,
            double range
    ) {
        return pointLight(originX, originY, originZ, luminance,
                blockX, blockY, blockZ, range, AttenuationCurve.QUADRATIC);
    }

    public static double pointLight(
            double originX, double originY, double originZ,
            double luminance,
            int blockX, int blockY, int blockZ,
            double range,
            AttenuationCurve curve
    ) {
        double dx = blockX + 0.5 - originX;
        double dy = blockY + 0.5 - originY;
        double dz = blockZ + 0.5 - originZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        if (distanceSquared >= range * range) {
            return 0.0;
        }
        if (distanceSquared < 1.0E-8) {
            return Math.max(luminance, 0.0);
        }
        return attenuatedIntensity(luminance, Math.sqrt(distanceSquared), range, curve);
    }

    public static double conePadding(double distance, double outerAngleTangent, double minimum, double maximum) {
        double padding = distance * outerAngleTangent;
        return Math.max(minimum, Math.min(maximum, padding));
    }

    /**
     * Computes the conservative AABB of a spherical cone sector. The sector
     * includes the apex, has the supplied radius, and is limited to the
     * forward hemisphere because light queries reject non-positive dot
     * products. The returned bounds are double coordinates; callers should
     * apply their voxelisation policy after receiving them.
     */
    public static Aabb sphericalConeBounds(
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double radius, double outerAngle
    ) {
        if (radius <= 0.0) {
            return new Aabb(originX, originY, originZ, originX, originY, originZ);
        }

        double directionLength = Math.sqrt(
                directionX * directionX
                        + directionY * directionY
                        + directionZ * directionZ
        );
        if (directionLength <= 1.0E-8) {
            return new Aabb(
                    originX - radius, originY - radius, originZ - radius,
                    originX + radius, originY + radius, originZ + radius
            );
        }

        double axisX = directionX / directionLength;
        double axisY = directionY / directionLength;
        double axisZ = directionZ / directionLength;
        double halfAngle = Math.min(Math.max(outerAngle, 0.0), Math.PI * 0.5);
        double sinAngle = Math.sin(halfAngle);
        double cosAngle = Math.cos(halfAngle);

        return new Aabb(
                originX + radius * Math.min(0.0, minDirectionalComponent(axisX, sinAngle, cosAngle)),
                originY + radius * Math.min(0.0, minDirectionalComponent(axisY, sinAngle, cosAngle)),
                originZ + radius * Math.min(0.0, minDirectionalComponent(axisZ, sinAngle, cosAngle)),
                originX + radius * Math.max(0.0, maxDirectionalComponent(axisX, sinAngle, cosAngle)),
                originY + radius * Math.max(0.0, maxDirectionalComponent(axisY, sinAngle, cosAngle)),
                originZ + radius * Math.max(0.0, maxDirectionalComponent(axisZ, sinAngle, cosAngle))
        );
    }

    private static double maxDirectionalComponent(double axisComponent, double sinAngle, double cosAngle) {
        if (axisComponent >= cosAngle) {
            return 1.0;
        }
        double perpendicular = Math.sqrt(Math.max(0.0, 1.0 - axisComponent * axisComponent));
        return axisComponent * cosAngle + perpendicular * sinAngle;
    }

    private static double minDirectionalComponent(double axisComponent, double sinAngle, double cosAngle) {
        if (-axisComponent >= cosAngle) {
            return -1.0;
        }
        double perpendicular = Math.sqrt(Math.max(0.0, 1.0 - axisComponent * axisComponent));
        return axisComponent * cosAngle - perpendicular * sinAngle;
    }

    public static long volume(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        long width = (long) endX - startX + 1;
        long height = (long) endY - startY + 1;
        long depth = (long) endZ - startZ + 1;
        return width * height * depth;
    }

    public record Aabb(
            double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ
    ) {
    }

    public static final class Direction {
        private final double x;
        private final double y;
        private final double z;

        private Direction(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public double z() {
            return z;
        }
    }
}
