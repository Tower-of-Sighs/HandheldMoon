package com.sighs.handheldmoon.registry;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_FIXED_FLASHLIGHT;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYER_RAY;
    public static final ModConfigSpec.ConfigValue<Boolean> REAL_LIGHT;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_INTENSITY;
    public static final ModConfigSpec.ConfigValue<Boolean> LIGHT_OCCLUSION;

    static {
        BUILDER.push("Client Setting");

        ENABLE_FIXED_FLASHLIGHT = BUILDER
                .comment("是否固定手电筒光的位置。")
                .define("enableFixedFlashlight", false);
        PLAYER_RAY = BUILDER
                .comment("是否启用其它玩家光束可见。")
                .define("enablePlayerRay", true);
        REAL_LIGHT = BUILDER
                .comment("是否启用真实照明。")
                .define("enableRealLight", true);
        LIGHT_INTENSITY = BUILDER
                .comment("手电筒光强度。")
                .defineInRange("lightIntensity", 0.3, 0.0, 1.0);
        LIGHT_OCCLUSION = BUILDER
                .comment("启用光照遮挡，性能敏感，复杂地形慎用")
                .define("enableLightOcclusion", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}