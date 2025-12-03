package com.sighs.handheldmoon.registry;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static volatile double ENGINE_FALLOFF_SCALE = 0.25;
    private static volatile double LAST_INTENSITY = Double.NaN;
    private static volatile long LAST_REFRESH_MS = 0L;

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_FIXED_FLASHLIGHT;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_INTENSITY;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYER_RAY;
    public static final ModConfigSpec.ConfigValue<Boolean> REAL_LIGHT;

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
                .define("LightIntensity", 0.3);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static double engineFalloffScale() {
        long now = System.currentTimeMillis();
        if (now - LAST_REFRESH_MS < 100) return ENGINE_FALLOFF_SCALE;
        LAST_REFRESH_MS = now;
        double base = 0.3;
        double intensity = LIGHT_INTENSITY.get();
        if (intensity == LAST_INTENSITY) return ENGINE_FALLOFF_SCALE;
        LAST_INTENSITY = intensity;
        if (intensity < 0.0) intensity = 0.0;
        double factor = 0.25 * Math.sqrt(intensity / base);
        if (factor < 0.05) factor = 0.05;
        if (factor > 1.0) factor = 1.0;
        ENGINE_FALLOFF_SCALE = factor;
        return factor;
    }
}