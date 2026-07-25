package cc.sighs.handheldmoon.config;

import java.util.Objects;
import java.util.function.Supplier;

public final class FullMoonDeviceConfig {
    private static volatile Supplier<FullMoonDeviceConfig> globalConfigSupplier =
            () -> new FullMoonDeviceConfig(true, 15.0, false);

    private final boolean realLight;
    private final double realLightLuminance;
    private final boolean lightOcclusion;

    public FullMoonDeviceConfig(boolean realLight, double realLightLuminance, boolean lightOcclusion) {
        this.realLight = realLight;
        this.realLightLuminance = clamp(realLightLuminance, 0.0, 15.0);
        this.lightOcclusion = lightOcclusion;
    }

    public boolean realLight() {
        return realLight;
    }

    public double realLightLuminance() {
        return realLightLuminance;
    }

    public boolean lightOcclusion() {
        return lightOcclusion;
    }

    public static FullMoonDeviceConfig fromGlobalConfig() {
        return globalConfigSupplier.get();
    }

    public static void setGlobalConfigSupplier(Supplier<FullMoonDeviceConfig> supplier) {
        globalConfigSupplier = Objects.requireNonNull(supplier);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FullMoonDeviceConfig)) {
            return false;
        }
        FullMoonDeviceConfig that = (FullMoonDeviceConfig) other;
        return realLight == that.realLight
                && Double.compare(realLightLuminance, that.realLightLuminance) == 0
                && lightOcclusion == that.lightOcclusion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(realLight, realLightLuminance, lightOcclusion);
    }

    @Override
    public String toString() {
        return "FullMoonDeviceConfig[realLight=" + realLight
                + ", realLightLuminance=" + realLightLuminance
                + ", lightOcclusion=" + lightOcclusion + ']';
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
