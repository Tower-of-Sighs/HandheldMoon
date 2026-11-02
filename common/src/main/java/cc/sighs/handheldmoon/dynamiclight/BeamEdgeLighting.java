package cc.sighs.handheldmoon.dynamiclight;

/** Beam-edge sampling shared by the Minecraft and loader adapters. Java 17 compatible. */
public final class BeamEdgeLighting {
    private static final double DONOR_LEVEL = 3.0;
    private static final double SUPPORT_LEVEL = 3.5;
    private static final double EDGE_TAPER = 0.9;

    private BeamEdgeLighting() { }

    @FunctionalInterface
    public interface LightSampler {
        /** Returns raw dynamic light, before edge filtering. */
        double sample(int x, int y, int z);
    }

    public static int apply(int x, int y, int z, int packedLight, LightSampler light) {
        int vanillaCoord = packedLight & 0xFFFF;
        double dynamic = light.sample(x, y, z);
        if (dynamic <= vanillaCoord / 16.0) {
            // A nonzero sample prevents smooth lighting from replacing darkness
            // with a bright diagonal donor at the beam rim.
            if (vanillaCoord == 0 && hasDonorNeighbour(x, y, z, light)) {
                return (packedLight & 0xFFFF0000) | 16;
            }
            return packedLight;
        }

        int supported = 0;
        if (light.sample(x + 1, y, z) >= SUPPORT_LEVEL) supported++;
        if (light.sample(x - 1, y, z) >= SUPPORT_LEVEL) supported++;
        if (light.sample(x, y, z + 1) >= SUPPORT_LEVEL) supported++;
        if (light.sample(x, y, z - 1) >= SUPPORT_LEVEL) supported++;
        int dynamicCoord = (int) (Math.min(dynamic, 15.0) * 16.0);
        dynamicCoord = (int) (dynamicCoord * (1.0 - EDGE_TAPER * (1.0 - supported / 4.0)));
        // Taper only the dynamic contribution: a nearby torch must never dim.
        return (packedLight & 0xFFFF0000) | Math.max(vanillaCoord, dynamicCoord);
    }

    private static boolean hasDonorNeighbour(int x, int y, int z, LightSampler light) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if ((dx != 0 || dy != 0 || dz != 0)
                            && light.sample(x + dx, y + dy, z + dz) >= DONOR_LEVEL) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
