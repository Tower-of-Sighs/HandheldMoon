package cc.sighs.handheldmoon.config;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.util.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class LampDeviceConfig {
    /** Default dynamic-light luminance of a flashlight item. */
    public static final double DEFAULT_REAL_LIGHT_INTENSITY = 15.0;
    /** Default post-processing brightness of a flashlight item. */
    public static final double DEFAULT_FILTER_LIGHT_INTENSITY = 0.3;
    /** Default visibility of a flashlight item's rendered cone. */
    public static final boolean DEFAULT_ENABLE_VISIBLE_CONE = true;
    /** Sentinel for a flashlight without an installed color lens. */
    public static final int NO_LENS_COLOR = -1;
    /** Default strength with which an installed color lens tints the beam. */
    public static final double DEFAULT_LENS_TINT_STRENGTH = 1.0;

    private static volatile Supplier<LampDeviceConfig> globalConfigSupplier = LampDeviceConfig::builtInDefaults;
    private static final Object GLOBAL_CONFIG_LOCK = new Object();
    private static volatile LampDeviceConfig globalConfigCache;

    private final double lightAngle;
    private final List<String> lightColorsARGB;
    private final boolean realLight;
    private final double filterLightIntensity;
    private final boolean lightOcclusion;
    private final double realLightLuminance;
    private final List<String> layerSizeScales;
    private final List<String> layerEdgeAlphas;
    private final List<String> layerColorsARGB;
    private final double colorNoiseAmplitude;
    private final FogSettings fog;
    private final double realLightIntensity;
    private final boolean enableVisibleCone;
    private final int lensColor;
    private final double lensTintStrength;

    public LampDeviceConfig(
            double lightAngle,
            List<String> lightColorsARGB,
            boolean realLight,
            double filterLightIntensity,
            boolean lightOcclusion,
            double realLightLuminance,
            List<String> layerSizeScales,
            List<String> layerEdgeAlphas,
            List<String> layerColorsARGB,
            double colorNoiseAmplitude,
            FogSettings fog,
            double realLightIntensity,
            boolean enableVisibleCone,
            int lensColor,
            double lensTintStrength
    ) {
        this.lightAngle = clamp(lightAngle, 10.0, 120.0);
        this.lightColorsARGB = immutableCopy(lightColorsARGB);
        this.realLight = realLight;
        this.filterLightIntensity = clamp(filterLightIntensity, 0.0, 1.0);
        this.lightOcclusion = lightOcclusion;
        this.realLightLuminance = clamp(realLightLuminance, 0.0, 15.0);
        this.layerSizeScales = immutableCopy(layerSizeScales);
        this.layerEdgeAlphas = immutableCopy(layerEdgeAlphas);
        this.layerColorsARGB = immutableCopy(layerColorsARGB);
        this.colorNoiseAmplitude = clamp(colorNoiseAmplitude, 0.0, 1.0);
        this.fog = fog;
        this.realLightIntensity = clamp(realLightIntensity, 0.0, 15.0);
        this.enableVisibleCone = enableVisibleCone;
        this.lensColor = lensColor < 0 ? NO_LENS_COLOR : lensColor & 0xFFFFFF;
        this.lensTintStrength = clamp(lensTintStrength, 0.0, 1.0);
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

    /** Post-processing brightness of the flashlight beam, in the range 0..1. */
    public double filterLightIntensity() {
        return filterLightIntensity;
    }

    /** Dynamic-light luminance of the flashlight item, in the range 0..15. */
    public double realLightIntensity() {
        return realLightIntensity;
    }

    /** Whether this device renders its visible light cone. */
    public boolean enableVisibleCone() {
        return enableVisibleCone;
    }

    /** Installed lens color as 0xRRGGBB, or {@link #NO_LENS_COLOR} when empty. */
    public int lensColor() {
        return lensColor;
    }

    public boolean hasLens() {
        return lensColor != NO_LENS_COLOR;
    }

    /** Strength in the range 0..1 with which the installed lens tints the beam. */
    public double lensTintStrength() {
        return lensTintStrength;
    }

    public boolean lightOcclusion() {
        return lightOcclusion;
    }

    public double realLightLuminance() {
        return realLightLuminance;
    }

    public List<String> layerSizeScales() {
        return layerSizeScales;
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

    /** Returns a copy of this config with {@code realLight} replaced. */
    public LampDeviceConfig withRealLight(boolean realLight) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
    }

    /** Returns a copy of this config with {@code filterLightIntensity} replaced. */
    public LampDeviceConfig withFilterLightIntensity(double filterLightIntensity) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
    }

    /** Returns a copy of this config with {@code realLightIntensity} replaced. */
    public LampDeviceConfig withRealLightIntensity(double realLightIntensity) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
    }

    /** Returns a copy of this config with {@code enableVisibleCone} replaced. */
    public LampDeviceConfig withEnableVisibleCone(boolean enableVisibleCone) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
    }

    /** Returns a copy of this config with {@code lensColor} replaced. */
    public LampDeviceConfig withLensColor(int lensColor) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
    }

    /** Returns a copy of this config with {@code lensTintStrength} replaced. */
    public LampDeviceConfig withLensTintStrength(double lensTintStrength) {
        return new LampDeviceConfig(
                lightAngle, lightColorsARGB, realLight, filterLightIntensity, lightOcclusion,
                realLightLuminance, layerSizeScales,
                layerEdgeAlphas, layerColorsARGB, colorNoiseAmplitude, fog,
                realLightIntensity, enableVisibleCone, lensColor, lensTintStrength);
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
        return Double.compare(lightAngle, that.lightAngle) == 0
                && realLight == that.realLight
                && Double.compare(filterLightIntensity, that.filterLightIntensity) == 0
                && Double.compare(realLightIntensity, that.realLightIntensity) == 0
                && enableVisibleCone == that.enableVisibleCone
                && lensColor == that.lensColor
                && Double.compare(lensTintStrength, that.lensTintStrength) == 0
                && lightOcclusion == that.lightOcclusion
                && Double.compare(realLightLuminance, that.realLightLuminance) == 0
                && Double.compare(colorNoiseAmplitude, that.colorNoiseAmplitude) == 0
                && lightColorsARGB.equals(that.lightColorsARGB)
                && layerSizeScales.equals(that.layerSizeScales)
                && layerEdgeAlphas.equals(that.layerEdgeAlphas)
                && layerColorsARGB.equals(that.layerColorsARGB)
                && Objects.equals(fog, that.fog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                lightAngle,
                lightColorsARGB,
                realLight,
                filterLightIntensity,
                realLightIntensity,
                enableVisibleCone,
                lensColor,
                lensTintStrength,
                lightOcclusion,
                realLightLuminance,
                layerSizeScales,
                layerEdgeAlphas,
                layerColorsARGB,
                colorNoiseAmplitude,
                fog
        );
    }

    /** Returns the built-in default device config, used as the single source of defaults. */
    public static LampDeviceConfig builtInDefaults() {
        return new LampDeviceConfig(
                56.0,
                Collections.singletonList(ColorUtils.webColorToRgbaHex(EntityLightProfile.DEFAULT_LIGHT_COLOR)),
                true,
                DEFAULT_FILTER_LIGHT_INTENSITY,
                false,
                15.0,
                Arrays.asList("1.00", "1.08", "1.16"),
                Arrays.asList("0.00", "0.00", "0.00"),
                Collections.emptyList(),
                0.35,
                new FogSettings(false, 1.30, 0.06, 0.05, "FFFFFF80"),
                DEFAULT_REAL_LIGHT_INTENSITY,
                DEFAULT_ENABLE_VISIBLE_CONE,
                NO_LENS_COLOR,
                DEFAULT_LENS_TINT_STRENGTH
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
        if (Double.isNaN(value) || value == Double.NEGATIVE_INFINITY) {
            return minimum;
        }
        if (value == Double.POSITIVE_INFINITY) {
            return maximum;
        }
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
