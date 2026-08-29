package cc.sighs.handheldmoon.config;

import java.util.Objects;
import java.util.function.Supplier;

public final class FullMoonDeviceConfig {
    private static volatile Supplier<FullMoonDeviceConfig> globalConfigSupplier =
            () -> new FullMoonDeviceConfig(true, 15.0, false);
    private static final Object GLOBAL_CONFIG_LOCK = new Object();
    private static volatile FullMoonDeviceConfig globalConfigCache;

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

    /** Returns the memoized global config; invalidate it after live values change. */
    public static FullMoonDeviceConfig fromGlobalConfig() {
        FullMoonDeviceConfig cached = globalConfigCache;
        if (cached != null) {
            return cached;
        }
        synchronized (GLOBAL_CONFIG_LOCK) {
            cached = globalConfigCache;
            if (cached == null) {
                cached = globalConfigSupplier.get();
                globalConfigCache = cached;
            }
            return cached;
        }
    }

    public static void setGlobalConfigSupplier(Supplier<FullMoonDeviceConfig> supplier) {
        synchronized (GLOBAL_CONFIG_LOCK) {
            globalConfigSupplier = Objects.requireNonNull(supplier);
            globalConfigCache = null;
        }
    }

    /** Invalidates the memoized global config after a live config value changes. */
    public static void invalidateGlobalConfig() {
        synchronized (GLOBAL_CONFIG_LOCK) {
            globalConfigCache = null;
        }
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
