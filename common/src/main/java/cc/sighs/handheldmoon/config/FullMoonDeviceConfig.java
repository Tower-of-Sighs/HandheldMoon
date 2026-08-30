package cc.sighs.handheldmoon.config;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import java.util.Objects;
import java.util.function.Supplier;

public final class FullMoonDeviceConfig {
    private static volatile Supplier<FullMoonDeviceConfig> globalConfigSupplier =
            () -> new FullMoonDeviceConfig(true, 15.0, false);
    private static final Object GLOBAL_CONFIG_LOCK = new Object();
    private static volatile FullMoonDeviceConfig globalConfigCache;

    private final boolean realLight;
    private final double realLightLuminance;
    private final double realLightRadius;
    private final AttenuationCurve realLightAttenuation;
    private final boolean lightOcclusion;

    public FullMoonDeviceConfig(boolean realLight, double realLightLuminance, boolean lightOcclusion) {
        this(realLight, realLightLuminance, DynamicLightDefaults.FULL_MOON_RANGE,
                AttenuationCurve.QUADRATIC, lightOcclusion);
    }

    public FullMoonDeviceConfig(boolean realLight, double realLightLuminance,
                                double realLightRadius, AttenuationCurve realLightAttenuation,
                                boolean lightOcclusion) {
        this.realLight = realLight;
        this.realLightLuminance = clamp(realLightLuminance, 0.0, 15.0);
        this.realLightRadius = clamp(realLightRadius, 0.0, 64.0);
        this.realLightAttenuation = realLightAttenuation == null
                ? AttenuationCurve.QUADRATIC : realLightAttenuation;
        this.lightOcclusion = lightOcclusion;
    }

    public boolean realLight() {
        return realLight;
    }

    public double realLightLuminance() {
        return realLightLuminance;
    }

    public double realLightRadius() {
        return realLightRadius;
    }

    public AttenuationCurve realLightAttenuation() {
        return realLightAttenuation;
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
                && Double.compare(realLightRadius, that.realLightRadius) == 0
                && realLightAttenuation == that.realLightAttenuation
                && lightOcclusion == that.lightOcclusion;
    }

    @Override
    public int hashCode() {
        return Objects.hash(realLight, realLightLuminance, realLightRadius,
                realLightAttenuation, lightOcclusion);
    }

    @Override
    public String toString() {
        return "FullMoonDeviceConfig[realLight=" + realLight
                + ", realLightLuminance=" + realLightLuminance
                + ", realLightRadius=" + realLightRadius
                + ", realLightAttenuation=" + realLightAttenuation
                + ", lightOcclusion=" + lightOcclusion + ']';
    }

    private static double clamp(double value, double minimum, double maximum) {
        if (Double.isNaN(value) || value == Double.NEGATIVE_INFINITY) {
            return minimum;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return maximum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }
}
