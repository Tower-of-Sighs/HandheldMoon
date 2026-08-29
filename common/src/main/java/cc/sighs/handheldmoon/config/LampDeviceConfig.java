package cc.sighs.handheldmoon.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class LampDeviceConfig {
    private static volatile Supplier<LampDeviceConfig> globalConfigSupplier = LampDeviceConfig::builtInDefaults;
    private static final Object GLOBAL_CONFIG_LOCK = new Object();
    private static volatile LampDeviceConfig globalConfigCache;

    private final double lightRange;
    private final double lightAngle;
    private final List<String> lightColorsARGB;
    private final boolean realLight;
    private final double lightIntensity;
    private final boolean lightOcclusion;
    private final boolean coneRaycast;
    private final double realLightLuminance;
    private final List<String> layerSizeScales;
    private final List<String> layerCenterAlphas;
    private final List<String> layerEdgeAlphas;
    private final List<String> layerColorsARGB;
    private final double colorNoiseAmplitude;
    private final FogSettings fog;

    public LampDeviceConfig(
            double lightRange,
            double lightAngle,
            List<String> lightColorsARGB,
            boolean realLight,
            double lightIntensity,
            boolean lightOcclusion,
            boolean coneRaycast,
            double realLightLuminance,
            List<String> layerSizeScales,
            List<String> layerCenterAlphas,
            List<String> layerEdgeAlphas,
            List<String> layerColorsARGB,
            double colorNoiseAmplitude,
            FogSettings fog
    ) {
        this.lightRange = clamp(lightRange, 1.0, 64.0);
        this.lightAngle = clamp(lightAngle, 10.0, 120.0);
        this.lightColorsARGB = immutableCopy(lightColorsARGB);
        this.realLight = realLight;
        this.lightIntensity = clamp(lightIntensity, 0.0, 1.0);
        this.lightOcclusion = lightOcclusion;
        this.coneRaycast = coneRaycast;
        this.realLightLuminance = clamp(realLightLuminance, 0.0, 15.0);
        this.layerSizeScales = immutableCopy(layerSizeScales);
        this.layerCenterAlphas = immutableCopy(layerCenterAlphas);
        this.layerEdgeAlphas = immutableCopy(layerEdgeAlphas);
        this.layerColorsARGB = immutableCopy(layerColorsARGB);
        this.colorNoiseAmplitude = clamp(colorNoiseAmplitude, 0.0, 1.0);
        this.fog = fog;
    }

    public double lightRange() {
        return lightRange;
    }

    public double lightAngle() {
        return lightAngle;
    }

    public List<String> lightColorsARGB() {
        return lightColorsARGB;
    }

    public boolean realLight() {
        return realLight;
    }

    public double lightIntensity() {
        return lightIntensity;
    }

    public boolean lightOcclusion() {
        return lightOcclusion;
    }

    public boolean coneRaycast() {
        return coneRaycast;
    }

    public double realLightLuminance() {
        return realLightLuminance;
    }

    public List<String> layerSizeScales() {
        return layerSizeScales;
    }

    public List<String> layerCenterAlphas() {
        return layerCenterAlphas;
    }

    public List<String> layerEdgeAlphas() {
        return layerEdgeAlphas;
    }

    public List<String> layerColorsARGB() {
        return layerColorsARGB;
    }

    public double colorNoiseAmplitude() {
        return colorNoiseAmplitude;
    }

    public FogSettings fog() {
        return fog;
    }

    /** Returns the memoized global config; invalidate it after live values change. */
    public static LampDeviceConfig fromGlobalConfig() {
        LampDeviceConfig cached = globalConfigCache;
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

    public static void setGlobalConfigSupplier(Supplier<LampDeviceConfig> supplier) {
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
        if (!(other instanceof LampDeviceConfig)) {
            return false;
        }
        LampDeviceConfig that = (LampDeviceConfig) other;
        return Double.compare(lightRange, that.lightRange) == 0
                && Double.compare(lightAngle, that.lightAngle) == 0
                && realLight == that.realLight
                && Double.compare(lightIntensity, that.lightIntensity) == 0
                && lightOcclusion == that.lightOcclusion
                && coneRaycast == that.coneRaycast
                && Double.compare(realLightLuminance, that.realLightLuminance) == 0
                && Double.compare(colorNoiseAmplitude, that.colorNoiseAmplitude) == 0
                && lightColorsARGB.equals(that.lightColorsARGB)
                && layerSizeScales.equals(that.layerSizeScales)
                && layerCenterAlphas.equals(that.layerCenterAlphas)
                && layerEdgeAlphas.equals(that.layerEdgeAlphas)
                && layerColorsARGB.equals(that.layerColorsARGB)
                && Objects.equals(fog, that.fog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                lightRange,
                lightAngle,
                lightColorsARGB,
                realLight,
                lightIntensity,
                lightOcclusion,
                coneRaycast,
                realLightLuminance,
                layerSizeScales,
                layerCenterAlphas,
                layerEdgeAlphas,
                layerColorsARGB,
                colorNoiseAmplitude,
                fog
        );
    }

    private static LampDeviceConfig builtInDefaults() {
        return new LampDeviceConfig(
                14.0,
                56.0,
                Collections.singletonList("FFFFFFFF"),
                true,
                0.3,
                false,
                false,
                15.0,
                Arrays.asList("1.00", "1.08", "1.16"),
                Arrays.asList("0.15", "0.12", "0.08"),
                Arrays.asList("0.00", "0.00", "0.00"),
                Collections.emptyList(),
                0.35,
                new FogSettings(false, 1.30, 0.06, 0.05, "80FFFFFF")
        );
    }

    private static List<String> immutableCopy(List<String> values) {
        List<String> copy = new ArrayList<>();
        for (String value : Objects.requireNonNull(values)) {
            copy.add(Objects.requireNonNull(value));
        }
        return Collections.unmodifiableList(copy);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    public static final class FogSettings {
        private final boolean enabled;
        private final double sizeScale;
        private final double centerAlpha;
        private final double edgeAlpha;
        private final String colorARGB;

        public FogSettings(boolean enabled, double sizeScale, double centerAlpha, double edgeAlpha, String colorARGB) {
            this.enabled = enabled;
            this.sizeScale = clamp(sizeScale, 1.0, 2.0);
            this.centerAlpha = clamp(centerAlpha, 0.0, 1.0);
            this.edgeAlpha = clamp(edgeAlpha, 0.0, 1.0);
            this.colorARGB = colorARGB;
        }

        public boolean enabled() {
            return enabled;
        }

        public double sizeScale() {
            return sizeScale;
        }

        public double centerAlpha() {
            return centerAlpha;
        }

        public double edgeAlpha() {
            return edgeAlpha;
        }

        public String colorARGB() {
            return colorARGB;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof FogSettings)) {
                return false;
            }
            FogSettings that = (FogSettings) other;
            return enabled == that.enabled
                    && Double.compare(sizeScale, that.sizeScale) == 0
                    && Double.compare(centerAlpha, that.centerAlpha) == 0
                    && Double.compare(edgeAlpha, that.edgeAlpha) == 0
                    && Objects.equals(colorARGB, that.colorARGB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(enabled, sizeScale, centerAlpha, edgeAlpha, colorARGB);
        }
    }
}
