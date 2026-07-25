package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
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

    private static final String TRANSLATE_KEY = "config.handheldmoon.client_settings";

    private static String translateKey(String key) {
        return TRANSLATE_KEY + "." + key;
    }

    static {
        BUILDER.push("Client Setting")
                .translation(TRANSLATE_KEY);

        ENABLE_FIXED_FLASHLIGHT = BUILDER
                .comment("是否固定手电筒光的位置。")
                .translation(translateKey("enableFixedFlashlight"))
                .define("enableFixedFlashlight", false);
        PLAYER_RAY = BUILDER
                .comment("是否启用其它玩家光束可见。")
                .translation(translateKey("enablePlayerRay"))
                .define("enablePlayerRay", true);
        LIGHT_RANGE = BUILDER
                .comment("手电筒光照范围（方块）。")
                .translation(translateKey("lightRange"))
                .defineInRange("lightRange", 14.0, 1.0, 64.0);
        LIGHT_ANGLE = BUILDER
                .comment("手电筒光照角度（度），控制光锥张开程度。")
                .translation(translateKey("lightAngle"))
                .defineInRange("lightAngle", 56.0, 10.0, 120.0);
        LIGHT_COLORS_ARGB = BUILDER
                .comment("手电筒光锥颜色列表（ARGB 十六进制，如 \"FFFFFFFF\"、\"80FF0000\"）。支持多色柔和渐变。")
                .translation(translateKey("lightColorsARGB"))
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
                .translation(translateKey("layerSizeScales"))
                .defineList("layerSizeScales",
                        List.of("1.00", "1.08", "1.16"),
                        () -> "1.00",
                        o -> o instanceof String);
        LAYER_CENTER_ALPHAS = BUILDER
                .comment("多层中心透明度列表（字符串表示浮点数 0–1），默认：0.15,0.12,0.08")
                .translation(translateKey("layerCenterAlphas"))
                .defineList("layerCenterAlphas",
                        List.of("0.15", "0.12", "0.08"),
                        () -> "0.10",
                        o -> o instanceof String);
        LAYER_EDGE_ALPHAS = BUILDER
                .comment("多层边缘透明度列表（字符串表示浮点数 0–1），默认：0.00,0.00,0.00")
                .translation(translateKey("layerEdgeAlphas"))
                .defineList("layerEdgeAlphas",
                        List.of("0.00", "0.00", "0.00"),
                        () -> "0.00",
                        o -> o instanceof String);
        LAYER_COLORS_ARGB = BUILDER
                .comment("每层基础颜色列表（ARGB 十六进制），可为空；为空时使用渐变调色与噪声混色")
                .translation(translateKey("layerColorsARGB"))
                .defineList("layerColorsARGB",
                        List.of(),
                        () -> "FFFFFFFF",
                        o -> o instanceof String);
        COLOR_NOISE_AMPLITUDE = BUILDER
                .comment("不规则渐变噪声幅度（0–1）")
                .translation(translateKey("colorNoiseAmplitude"))
                .defineInRange("colorNoiseAmplitude", 0.35, 0.0, 1.0);
        FOG_ENABLED = BUILDER
                .comment("是否启用雾气层")
                .translation(translateKey("fogEnabled"))
                .define("fogEnabled", false);
        FOG_SIZE_SCALE = BUILDER
                .comment("雾气层尺寸缩放")
                .translation(translateKey("fogSizeScale"))
                .defineInRange("fogSizeScale", 1.30, 1.0, 2.0);
        FOG_CENTER_ALPHA = BUILDER
                .comment("雾气层中心透明度（0–1）")
                .translation(translateKey("fogCenterAlpha"))
                .defineInRange("fogCenterAlpha", 0.06, 0.0, 1.0);
        FOG_EDGE_ALPHA = BUILDER
                .comment("雾气层边缘透明度（0–1）")
                .translation(translateKey("fogEdgeAlpha"))
                .defineInRange("fogEdgeAlpha", 0.05, 0.0, 1.0);
        FOG_COLOR_ARGB = BUILDER
                .comment("雾气层颜色（ARGB 十六进制），默认半透明白：80FFFFFF")
                .translation(translateKey("fogColorARGB"))
                .define("fogColorARGB", "80FFFFFF");
        REAL_LIGHT = BUILDER
                .comment("是否启用真实照明。")
                .translation(translateKey("enableRealLight"))
                .define("enableRealLight", true);
        REAL_LIGHT_LUMINANCE = BUILDER
                .comment("真实光照等级（0–15），影响亮度与覆盖范围。")
                .translation(translateKey("realLightLuminance"))
                .defineInRange("realLightLuminance", 15.0, 0.0, 15.0);
        LIGHT_INTENSITY = BUILDER
                .comment("手电筒光强度。")
                .translation(translateKey("lightIntensity"))
                .defineInRange("lightIntensity", 0.3, 0.0, 1.0);
        LIGHT_OCCLUSION = BUILDER
                .comment("启用光照遮挡，性能敏感，复杂地形慎用")
                .translation(translateKey("enableLightOcclusion"))
                .define("enableLightOcclusion", false);
        CONE_RAYCAST = BUILDER
                .comment("启用圆锥渲染的射线检测（截断）。")
                .translation(translateKey("enableConeRaycast"))
                .define("enableConeRaycast", false);
        BUILDER.pop();

        SPEC = BUILDER.build();

        LampDeviceConfig.setGlobalConfigSupplier(() -> new LampDeviceConfig(
                LIGHT_RANGE.get(),
                LIGHT_ANGLE.get(),
                List.copyOf(LIGHT_COLORS_ARGB.get()),
                REAL_LIGHT.get(),
                LIGHT_INTENSITY.get(),
                LIGHT_OCCLUSION.get(),
                CONE_RAYCAST.get(),
                REAL_LIGHT_LUMINANCE.get(),
                List.copyOf(LAYER_SIZE_SCALES.get()),
                List.copyOf(LAYER_CENTER_ALPHAS.get()),
                List.copyOf(LAYER_EDGE_ALPHAS.get()),
                List.copyOf(LAYER_COLORS_ARGB.get()),
                COLOR_NOISE_AMPLITUDE.get(),
                new LampDeviceConfig.FogSettings(
                        FOG_ENABLED.get(),
                        FOG_SIZE_SCALE.get(),
                        FOG_CENTER_ALPHA.get(),
                        FOG_EDGE_ALPHA.get(),
                        FOG_COLOR_ARGB.get()
                )
        ));
        FullMoonDeviceConfig.setGlobalConfigSupplier(() -> new FullMoonDeviceConfig(
                REAL_LIGHT.get(),
                REAL_LIGHT_LUMINANCE.get(),
                LIGHT_OCCLUSION.get()
        ));
    }
}
