package cc.sighs.handheldmoon.dynamiclight;

/** Per-mesh-worker scope: keep analytic flashlight light out of Sodium's voxel cache. */
public final class SodiumLightSampling {
    private static final ThreadLocal<Boolean> BLOCK_SAMPLES = ThreadLocal.withInitial(() -> false);

    private SodiumLightSampling() { }

    public static boolean isBlockSample() {
        return BLOCK_SAMPLES.get();
    }

    public static boolean beginBlockSamples() {
        boolean previous = BLOCK_SAMPLES.get();
        BLOCK_SAMPLES.set(true);
        return previous;
    }

    public static void endBlockSamples(boolean previous) {
        BLOCK_SAMPLES.set(previous);
    }
}
