package cc.sighs.handheldmoon.config;

import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class DeviceConfigCodecs {
    public static final Codec<FullMoonDeviceConfig> FULL_MOON = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("realLight").forGetter(FullMoonDeviceConfig::realLight),
            Codec.DOUBLE.fieldOf("realLightLuminance").forGetter(FullMoonDeviceConfig::realLightLuminance),
            Codec.DOUBLE.optionalFieldOf("realLightRadius", DynamicLightDefaults.FULL_MOON_RANGE)
                    .forGetter(FullMoonDeviceConfig::realLightRadius),
            Codec.STRING.xmap(AttenuationCurve::parse, AttenuationCurve::name)
                    .optionalFieldOf("realLightAttenuation", AttenuationCurve.QUADRATIC)
                    .forGetter(FullMoonDeviceConfig::realLightAttenuation),
            Codec.BOOL.fieldOf("lightOcclusion").forGetter(FullMoonDeviceConfig::lightOcclusion)
    ).apply(instance, FullMoonDeviceConfig::new));

    public static final Codec<LampDeviceConfig.FogSettings> FOG = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("enabled").forGetter(LampDeviceConfig.FogSettings::enabled),
            Codec.DOUBLE.fieldOf("sizeScale").forGetter(LampDeviceConfig.FogSettings::sizeScale),
            Codec.DOUBLE.fieldOf("centerAlpha").forGetter(LampDeviceConfig.FogSettings::centerAlpha),
            Codec.DOUBLE.fieldOf("edgeAlpha").forGetter(LampDeviceConfig.FogSettings::edgeAlpha),
            Codec.STRING.fieldOf("colorARGB").forGetter(LampDeviceConfig.FogSettings::colorARGB)
    ).apply(instance, LampDeviceConfig.FogSettings::new));

    public static final Codec<LampDeviceConfig> LAMP = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("lightRange").forGetter(LampDeviceConfig::lightRange),
            Codec.DOUBLE.fieldOf("lightAngle").forGetter(LampDeviceConfig::lightAngle),
            Codec.list(Codec.STRING).fieldOf("lightColorsARGB").forGetter(LampDeviceConfig::lightColorsARGB),
            Codec.BOOL.fieldOf("realLight").forGetter(LampDeviceConfig::realLight),
            Codec.DOUBLE.fieldOf("lightIntensity").forGetter(LampDeviceConfig::lightIntensity),
            Codec.BOOL.fieldOf("lightOcclusion").forGetter(LampDeviceConfig::lightOcclusion),
            Codec.BOOL.fieldOf("coneRaycast").forGetter(LampDeviceConfig::coneRaycast),
            Codec.DOUBLE.fieldOf("realLightLuminance").forGetter(LampDeviceConfig::realLightLuminance),
            Codec.DOUBLE.optionalFieldOf("realLightRadius", DynamicLightDefaults.FLASHLIGHT_RANGE)
                    .forGetter(LampDeviceConfig::realLightRadius),
            Codec.STRING.xmap(AttenuationCurve::parse, AttenuationCurve::name)
                    .optionalFieldOf("realLightAttenuation", AttenuationCurve.QUADRATIC)
                    .forGetter(LampDeviceConfig::realLightAttenuation),
            Codec.list(Codec.STRING).fieldOf("layerSizeScales").forGetter(LampDeviceConfig::layerSizeScales),
            Codec.list(Codec.STRING).fieldOf("layerCenterAlphas").forGetter(LampDeviceConfig::layerCenterAlphas),
            Codec.list(Codec.STRING).fieldOf("layerEdgeAlphas").forGetter(LampDeviceConfig::layerEdgeAlphas),
            Codec.list(Codec.STRING).fieldOf("layerColorsARGB").forGetter(LampDeviceConfig::layerColorsARGB),
            Codec.DOUBLE.fieldOf("colorNoiseAmplitude").forGetter(LampDeviceConfig::colorNoiseAmplitude),
            FOG.fieldOf("fog").forGetter(LampDeviceConfig::fog)
    ).apply(instance, LampDeviceConfig::new));

    private DeviceConfigCodecs() {
    }
}
