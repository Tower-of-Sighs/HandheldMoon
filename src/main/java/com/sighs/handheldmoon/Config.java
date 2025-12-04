package com.sighs.handheldmoon;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec CONFIG_SPEC;

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_FIXED_FLASHLIGHT;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_INTENSITY;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYER_RAY;
    public static final ModConfigSpec.ConfigValue<Boolean> REAL_LIGHT;

    static {
        ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
        CONFIG_BUILDER.push("config");
        ENABLE_FIXED_FLASHLIGHT = CONFIG_BUILDER
                .define("enableFixedFlashlight", false);
        PLAYER_RAY = CONFIG_BUILDER
                .define("enablePlayerRay", true);
        REAL_LIGHT = CONFIG_BUILDER
                .define("enableRealLight", true);
        LIGHT_INTENSITY = CONFIG_BUILDER
                .define("lightIntensity", 0.3);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}
