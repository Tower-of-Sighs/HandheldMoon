package cc.sighs.handheldmoon.config;

import cc.sighs.handheldmoon.registry.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.List;

public record LampDeviceConfig(
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
    public record FogSettings(
            boolean enabled,
            double sizeScale,
            double centerAlpha,
            double edgeAlpha,
            String colorARGB
    ) {
        public static final Codec<FogSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("enabled").forGetter(FogSettings::enabled),
                Codec.DOUBLE.fieldOf("sizeScale").forGetter(FogSettings::sizeScale),
                Codec.DOUBLE.fieldOf("centerAlpha").forGetter(FogSettings::centerAlpha),
                Codec.DOUBLE.fieldOf("edgeAlpha").forGetter(FogSettings::edgeAlpha),
                Codec.STRING.fieldOf("colorARGB").forGetter(FogSettings::colorARGB)
        ).apply(instance, FogSettings::new));

        public FogSettings {
            sizeScale = Mth.clamp(sizeScale, 1.0, 2.0);
            centerAlpha = Mth.clamp(centerAlpha, 0.0, 1.0);
            edgeAlpha = Mth.clamp(edgeAlpha, 0.0, 1.0);
        }
    }

    public static final Codec<LampDeviceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("lightRange").forGetter(LampDeviceConfig::lightRange),
            Codec.DOUBLE.fieldOf("lightAngle").forGetter(LampDeviceConfig::lightAngle),
            Codec.list(Codec.STRING).fieldOf("lightColorsARGB").forGetter(LampDeviceConfig::lightColorsARGB),
            Codec.BOOL.fieldOf("realLight").forGetter(LampDeviceConfig::realLight),
            Codec.DOUBLE.fieldOf("lightIntensity").forGetter(LampDeviceConfig::lightIntensity),
            Codec.BOOL.fieldOf("lightOcclusion").forGetter(LampDeviceConfig::lightOcclusion),
            Codec.BOOL.fieldOf("coneRaycast").forGetter(LampDeviceConfig::coneRaycast),
            Codec.DOUBLE.fieldOf("realLightLuminance").forGetter(LampDeviceConfig::realLightLuminance),
            Codec.list(Codec.STRING).fieldOf("layerSizeScales").forGetter(LampDeviceConfig::layerSizeScales),
            Codec.list(Codec.STRING).fieldOf("layerCenterAlphas").forGetter(LampDeviceConfig::layerCenterAlphas),
            Codec.list(Codec.STRING).fieldOf("layerEdgeAlphas").forGetter(LampDeviceConfig::layerEdgeAlphas),
            Codec.list(Codec.STRING).fieldOf("layerColorsARGB").forGetter(LampDeviceConfig::layerColorsARGB),
            Codec.DOUBLE.fieldOf("colorNoiseAmplitude").forGetter(LampDeviceConfig::colorNoiseAmplitude),
            FogSettings.CODEC.fieldOf("fog").forGetter(LampDeviceConfig::fog)
    ).apply(instance, LampDeviceConfig::new));

    public LampDeviceConfig {
        lightRange = Mth.clamp(lightRange, 1.0, 64.0);
        lightAngle = Mth.clamp(lightAngle, 10.0, 120.0);
        lightIntensity = Mth.clamp(lightIntensity, 0.0, 1.0);
        realLightLuminance = Mth.clamp(realLightLuminance, 0.0, 15.0);
        colorNoiseAmplitude = Mth.clamp(colorNoiseAmplitude, 0.0, 1.0);
        lightColorsARGB = List.copyOf(lightColorsARGB);
        layerSizeScales = List.copyOf(layerSizeScales);
        layerCenterAlphas = List.copyOf(layerCenterAlphas);
        layerEdgeAlphas = List.copyOf(layerEdgeAlphas);
        layerColorsARGB = List.copyOf(layerColorsARGB);
    }

    public static LampDeviceConfig fromGlobalConfig() {
        return new LampDeviceConfig(
                Config.LIGHT_RANGE.get(),
                Config.LIGHT_ANGLE.get(),
                Config.LIGHT_COLORS_ARGB.get(),
                Config.REAL_LIGHT.get(),
                Config.LIGHT_INTENSITY.get(),
                Config.LIGHT_OCCLUSION.get(),
                Config.CONE_RAYCAST.get(),
                Config.REAL_LIGHT_LUMINANCE.get(),
                Config.LAYER_SIZE_SCALES.get(),
                Config.LAYER_CENTER_ALPHAS.get(),
                Config.LAYER_EDGE_ALPHAS.get(),
                Config.LAYER_COLORS_ARGB.get(),
                Config.COLOR_NOISE_AMPLITUDE.get(),
                new FogSettings(
                        Config.FOG_ENABLED.get(),
                        Config.FOG_SIZE_SCALE.get(),
                        Config.FOG_CENTER_ALPHA.get(),
                        Config.FOG_EDGE_ALPHA.get(),
                        Config.FOG_COLOR_ARGB.get()
                )
        );
    }
}
