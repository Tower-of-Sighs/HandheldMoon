package cc.sighs.handheldmoon.util;

/** Pure light geometry shared by Minecraft-version adapters. */
public final class SharedLightMath {
    private static final double DEGREES_TO_RADIANS = Math.PI / 180.0;

    private SharedLightMath() {
    }

    public static Direction direction(float yawDegrees, float pitchDegrees, boolean lampMode) {
        double yaw = yawDegrees * DEGREES_TO_RADIANS;
        double pitch = pitchDegrees * DEGREES_TO_RADIANS;
        double horizontal = Math.cos(pitch);
        double x = Math.sin(yaw) * horizontal;
        if (!lampMode) {
            x = -x;
        }
        return new Direction(x, -Math.sin(pitch), Math.cos(yaw) * horizontal);
    }

    public static double effectiveRange(double luminance, double range, double threshold) {
        if (luminance <= threshold) {
            return 0.0;
        }
        double factor = 1.0 - threshold / luminance;
        return range * Math.sqrt(Math.max(0.0, factor));
    }

    public static double conePadding(double distance, double outerAngleRadians, double minimum, double maximum) {
        double padding = distance * Math.tan(outerAngleRadians);
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
