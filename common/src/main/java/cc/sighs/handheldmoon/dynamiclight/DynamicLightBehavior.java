package cc.sighs.handheldmoon.dynamiclight;

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
    }
}
