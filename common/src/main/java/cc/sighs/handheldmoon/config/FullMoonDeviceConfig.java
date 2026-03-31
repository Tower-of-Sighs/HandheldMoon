package cc.sighs.handheldmoon.config;

import cc.sighs.handheldmoon.registry.Config;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public record FullMoonDeviceConfig(
        boolean realLight,
        double realLightLuminance,
        boolean lightOcclusion
) {
    public static final Codec<FullMoonDeviceConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("realLight").forGetter(FullMoonDeviceConfig::realLight),
            Codec.DOUBLE.fieldOf("realLightLuminance").forGetter(FullMoonDeviceConfig::realLightLuminance),
            Codec.BOOL.fieldOf("lightOcclusion").forGetter(FullMoonDeviceConfig::lightOcclusion)
    ).apply(instance, FullMoonDeviceConfig::new));

    public FullMoonDeviceConfig {
        realLightLuminance = Mth.clamp(realLightLuminance, 0.0, 15.0);
    }

    public static FullMoonDeviceConfig fromGlobalConfig() {
        return new FullMoonDeviceConfig(
                Config.REAL_LIGHT.get(),
                Config.REAL_LIGHT_LUMINANCE.get(),
                Config.LIGHT_OCCLUSION.get()
        );
    }
}
