package cc.sighs.handheldmoon.dynamiclight;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.util.SharedLightMath;

/**
 * A client-side dynamic light source understood by the HandheldMoon engine.
 *
 * <p>The protocol deliberately uses primitive block coordinates so the common
 * module does not depend on Minecraft classes or a particular loader.</p>
 */
public interface DynamicLightBehavior {
    /** Returns the light contribution at a block coordinate. */
    double lightAt(int blockX, int blockY, int blockZ, double falloffRatio);

    /** Returns the current spatial bounds used for section invalidation. */
    Bounds getBounds();

    /** Returns whether the source moved or otherwise changed since its last query. */
    boolean hasChanged();

    /** Returns whether the source should be removed from the engine. */
    boolean isRemoved();

    /**
     * Captures a world-independent light snapshot for optional background
     * sampling. Sources that need world state (for example occlusion raycasts)
     * must return {@code null}.
     */
    default BatchLightSnapshot getBatchLightSnapshot() {
        return null;
    }

    /** Immutable, Minecraft-independent state used by section batch workers. */
    record BatchLightSnapshot(
            boolean cone,
            double originX,
            double originY,
            double originZ,
            double directionX,
            double directionY,
            double directionZ,
            double range,
            double luminance,
            double cosInner,
            double cosOuter,
            double cosOuterSq,
            AttenuationCurve attenuationCurve
    ) {
        public BatchLightSnapshot(
                boolean cone,
                double originX,
                double originY,
                double originZ,
                double directionX,
                double directionY,
                double directionZ,
                double range,
                double luminance,
                double cosInner,
                double cosOuter,
                double cosOuterSq
        ) {
            this(cone, originX, originY, originZ, directionX, directionY, directionZ,
                    range, luminance, cosInner, cosOuter, cosOuterSq,
                    AttenuationCurve.QUADRATIC);
        }

        public BatchLightSnapshot {
            attenuationCurve = attenuationCurve == null
                    ? AttenuationCurve.QUADRATIC : attenuationCurve;
        }

        public double lightAt(int blockX, int blockY, int blockZ) {
            if (cone) {
                return SharedLightMath.coneLight(
                        originX, originY, originZ,
                        directionX, directionY, directionZ,
                        luminance, blockX, blockY, blockZ, range,
                        cosInner, cosOuter, cosOuterSq, attenuationCurve
                );
            }

            return SharedLightMath.pointLight(
                    originX, originY, originZ, luminance,
                    blockX, blockY, blockZ, range, attenuationCurve
            );
        }
    }

    final class Bounds {
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;

        public Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            if (minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("Invalid dynamic-light bounds");
            }
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public static Bounds empty() {
            return new Bounds(0, 0, 0, 0, 0, 0);
        }

        public Bounds expand(Bounds other) {
            return new Bounds(
                    Math.min(minX, other.minX),
                    Math.min(minY, other.minY),
                    Math.min(minZ, other.minZ),
                    Math.max(maxX, other.maxX),
                    Math.max(maxY, other.maxY),
                    Math.max(maxZ, other.maxZ)
            );
        }

        public int minX() {
            return minX;
        }

        public int minY() {
            return minY;
        }

        public int minZ() {
            return minZ;
        }

        public int maxX() {
            return maxX;
        }

        public int maxY() {
            return maxY;
        }

        public int maxZ() {
            return maxZ;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Bounds bounds)) return false;
            return minX == bounds.minX && minY == bounds.minY && minZ == bounds.minZ
                    && maxX == bounds.maxX && maxY == bounds.maxY && maxZ == bounds.maxZ;
        }

        @Override
        public int hashCode() {
            int result = minX;
            result = 31 * result + minY;
            result = 31 * result + minZ;
            result = 31 * result + maxX;
            result = 31 * result + maxY;
            return 31 * result + maxZ;
        }

        @Override
        public String toString() {
            return "Bounds[" + minX + "," + minY + "," + minZ
                    + " -> " + maxX + "," + maxY + "," + maxZ + "]";
        }
    }
}
