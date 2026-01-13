package com.sighs.handheldmoon.registry;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class Config {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_FIXED_FLASHLIGHT;
    public static final ModConfigSpec.ConfigValue<Boolean> PLAYER_RAY;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_RANGE;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_ANGLE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHT_COLORS_ARGB;
    public static final ModConfigSpec.ConfigValue<Boolean> REAL_LIGHT;
    public static final ModConfigSpec.ConfigValue<Double> LIGHT_INTENSITY;
    public static final ModConfigSpec.ConfigValue<Boolean> LIGHT_OCCLUSION;
    public static final ModConfigSpec.ConfigValue<Boolean> CONE_RAYCAST;
    public static final ModConfigSpec.ConfigValue<Double> REAL_LIGHT_LUMINANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LAYER_SIZE_SCALES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LAYER_CENTER_ALPHAS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LAYER_EDGE_ALPHAS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> LAYER_COLORS_ARGB;
    public static final ModConfigSpec.ConfigValue<Double> COLOR_NOISE_AMPLITUDE;
    public static final ModConfigSpec.ConfigValue<Boolean> FOG_ENABLED;
    public static final ModConfigSpec.ConfigValue<Double> FOG_SIZE_SCALE;
    public static final ModConfigSpec.ConfigValue<Double> FOG_CENTER_ALPHA;
    public static final ModConfigSpec.ConfigValue<Double> FOG_EDGE_ALPHA;
    public static final ModConfigSpec.ConfigValue<String> FOG_COLOR_ARGB;

    private static final String TRANSLATE_KEY = "config.handheldMoon.client_settings";

    private static String translateKey(String key) {
        return TRANSLATE_KEY + "." + key;
    }

    static {
        BUILDER.push("Client Setting")
                .translation(TRANSLATE_KEY);

        ENABLE_FIXED_FLASHLIGHT = BUILDER
                .comment("是否固定手电筒光的位置。")
                .translation(translateKey("enable_fixed_flashlight"))
                .define("enableFixedFlashlight", false);
        PLAYER_RAY = BUILDER
                .comment("是否启用其它玩家光束可见。")
                .translation(translateKey("enable_player_ray"))
                .define("enablePlayerRay", true);
        LIGHT_RANGE = BUILDER
                .comment("手电筒光照范围（方块）。")
                .translation(translateKey("light_range"))
                .defineInRange("lightRange", 14.0, 1.0, 64.0);
        LIGHT_ANGLE = BUILDER
                .comment("手电筒光照角度（度），控制光锥张开程度。")
                .translation(translateKey("light_angle"))
                .defineInRange("lightAngle", 56.0, 10.0, 120.0);
        LIGHT_COLORS_ARGB = BUILDER
                .comment("手电筒光锥颜色列表（ARGB 十六进制，如 \"FFFFFFFF\"、\"80FF0000\"）。支持多色柔和渐变。")
                .translation(translateKey("light_colors_argb"))
                .defineList("lightColorsARGB",
                        List.of("FFFFFFFF"),
                        () -> "FFFFFFFF",
                        o -> {
                            if (!(o instanceof String s)) return false;
                            String t = s.startsWith("#") ? s.substring(1) : s;
                            if (t.length() < 8) return false;
                            for (int i = 0; i < t.length(); i++) {
                                int d = Character.digit(t.charAt(i), 16);
                                if (d == -1) return false;
                            }
                            return true;
                        });
        LAYER_SIZE_SCALES = BUILDER
                .comment("多层圆锥尺寸缩放列表（字符串表示浮点数），默认 3 层：1.00,1.08,1.16")
                .translation(translateKey("layer_size_scales"))
                .defineList("layerSizeScales",
                        List.of("1.00", "1.08", "1.16"),
                        () -> "1.00",
                        o -> o instanceof String);
        LAYER_CENTER_ALPHAS = BUILDER
                .comment("多层中心透明度列表（字符串表示浮点数 0–1），默认：0.15,0.12,0.08")
                .translation(translateKey("layer_center_alphas"))
                .defineList("layerCenterAlphas",
                        List.of("0.15", "0.12", "0.08"),
                        () -> "0.10",
                        o -> o instanceof String);
        LAYER_EDGE_ALPHAS = BUILDER
                .comment("多层边缘透明度列表（字符串表示浮点数 0–1），默认：0.00,0.00,0.00")
                .translation(translateKey("layer_edge_alphas"))
                .defineList("layerEdgeAlphas",
                        List.of("0.00", "0.00", "0.00"),
                        () -> "0.00",
                        o -> o instanceof String);
        LAYER_COLORS_ARGB = BUILDER
                .comment("每层基础颜色列表（ARGB 十六进制），可为空；为空时使用渐变调色与噪声混色")
                .translation(translateKey("layer_colors_argb"))
                .defineList("layerColorsARGB",
                        List.of(),
                        () -> "FFFFFFFF",
                        o -> o instanceof String);
        COLOR_NOISE_AMPLITUDE = BUILDER
                .comment("不规则渐变噪声幅度（0–1）")
                .translation(translateKey("color_noise_amplitude"))
                .defineInRange("colorNoiseAmplitude", 0.35, 0.0, 1.0);
        FOG_ENABLED = BUILDER
                .comment("是否启用雾气层")
                .translation(translateKey("fog_enabled"))
                .define("fogEnabled", false);
        FOG_SIZE_SCALE = BUILDER
                .comment("雾气层尺寸缩放")
                .translation(translateKey("fog_size_scale"))
                .defineInRange("fogSizeScale", 1.30, 1.0, 2.0);
        FOG_CENTER_ALPHA = BUILDER
                .comment("雾气层中心透明度（0–1）")
                .translation(translateKey("fog_center_alpha"))
                .defineInRange("fogCenterAlpha", 0.06, 0.0, 1.0);
        FOG_EDGE_ALPHA = BUILDER
                .comment("雾气层边缘透明度（0–1）")
                .translation(translateKey("fog_edge_alpha"))
                .defineInRange("fogEdgeAlpha", 0.05, 0.0, 1.0);
        FOG_COLOR_ARGB = BUILDER
                .comment("雾气层颜色（ARGB 十六进制），默认半透明白：80FFFFFF")
                .translation(translateKey("fog_color_argb"))
                .define("fogColorARGB", "80FFFFFF");
        REAL_LIGHT = BUILDER
                .comment("是否启用真实照明。")
                .translation(translateKey("enable_real_light"))
                .define("enableRealLight", true);
        REAL_LIGHT_LUMINANCE = BUILDER
                .comment("真实光照等级（0–15），影响亮度与覆盖范围。")
                .translation(translateKey("real_light_luminance"))
                .defineInRange("realLightLuminance", 15.0, 0.0, 15.0);
        LIGHT_INTENSITY = BUILDER
                .comment("手电筒光强度。")
                .translation(translateKey("light_intensity"))
                .defineInRange("lightIntensity", 0.3, 0.0, 1.0);
        LIGHT_OCCLUSION = BUILDER
                .comment("启用光照遮挡，性能敏感，复杂地形慎用")
                .translation(translateKey("enable_light_occlusion"))
                .define("enableLightOcclusion", false);
        CONE_RAYCAST = BUILDER
                .comment("启用圆锥渲染的射线检测（截断）。")
                .translation(translateKey("enable_cone_raycast"))
                .define("enableConeRaycast", false);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
