package cc.sighs.handheldmoon.util;

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
        if (luminance <= threshold) {
            return 0.0;
        }
        double factor = 1.0 - threshold / luminance;
        return range * Math.sqrt(Math.max(0.0, factor));
    }

    /**
     * Quadratic distance attenuation for Minecraft's discrete 0-15 light levels.
     * This stays visibly bright through the middle of the configured range and
     * matches the inverse used by {@link #effectiveRange(double, double, double)}.
     */
    public static double distanceAttenuation(double distance, double range) {
        if (range <= 0.0 || distance >= range) {
            return 0.0;
        }
        if (distance <= 0.0) {
            return 1.0;
        }
        double t = distance / range;
        return 1.0 - t * t;
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
