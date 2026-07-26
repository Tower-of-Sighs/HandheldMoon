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

    public static long volume(int startX, int startY, int startZ, int endX, int endY, int endZ) {
        long width = (long) endX - startX + 1;
        long height = (long) endY - startY + 1;
        long depth = (long) endZ - startZ + 1;
        return width * height * depth;
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
