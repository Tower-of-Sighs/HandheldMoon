package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.oelib.config.ConfigAccess;
import cc.sighs.oelib.config.ConfigManager;
import cc.sighs.oelib.config.ConfigRecordCodecBuilder;
import cc.sighs.oelib.config.ConfigUnit;
import cc.sighs.oelib.config.field.ConfigField;
import cc.sighs.oelib.config.model.ConfigStorageFormat;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class Config {
    private Config() {
    }

    public record FogConfig(
            boolean enabled,
            double sizeScale,
            double centerAlpha,
            double edgeAlpha,
            String colorARGB
    ) {
    }

    public record ClientConfig(
            boolean enableFixedFlashlight,
            boolean playerRay,
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
            FogConfig fog
    ) {
    }

    private static FogConfig defaultFog() {
        return new FogConfig(false, 1.30, 0.06, 0.05, "80FFFFFF");
    }

    private static Codec<FogConfig> fogCodec() {
        return RecordCodecBuilder.create(
                instance -> instance.group(
                        ConfigField.bool("fogEnabled")
                                .defaultValue(false)
                                .comment("Enable outer fog layer.")
                                .tooltip()
                                .forGetter(FogConfig::enabled),
                        ConfigField.doubleRange("fogSizeScale", 1.0, 2.0)
                                .defaultValue(1.30)
                                .comment("Fog size scale.")
                                .tooltip()
                                .forGetter(FogConfig::sizeScale),
                        ConfigField.doubleRange("fogCenterAlpha", 0.0, 1.0)
                                .defaultValue(0.06)
                                .comment("Fog center alpha.")
                                .tooltip()
                                .forGetter(FogConfig::centerAlpha),
                        ConfigField.doubleRange("fogEdgeAlpha", 0.0, 1.0)
                                .defaultValue(0.05)
                                .comment("Fog edge alpha.")
                                .tooltip()
                                .forGetter(FogConfig::edgeAlpha),
                        ConfigField.string("fogColorARGB")
                                .defaultValue("80FFFFFF")
                                .comment("Fog ARGB color.")
                                .tooltip()
                                .forGetter(FogConfig::colorARGB)
                ).apply(instance, FogConfig::new)
        );
    }

    public static final ConfigUnit<ClientConfig> UNIT = ConfigRecordCodecBuilder.createClient(
            Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "client_settings"),
            instance -> instance.group(
                    ConfigField.bool("enableFixedFlashlight")
                            .defaultValue(false)
                            .comment("Fix flashlight beam position in third person.")
                            .tooltip()
                            .forGetter(ClientConfig::enableFixedFlashlight),
                    ConfigField.bool("enablePlayerRay")
                            .defaultValue(true)
                            .comment("Render beams from other players.")
                            .tooltip()
                            .forGetter(ClientConfig::playerRay),
                    ConfigField.doubleRange("lightRange", 1.0, 64.0)
                            .defaultValue(14.0)
                            .comment("Maximum flashlight beam distance.")
                            .tooltip()
                            .forGetter(ClientConfig::lightRange),
                    ConfigField.doubleRange("lightAngle", 10.0, 120.0)
                            .defaultValue(56.0)
                            .comment("Flashlight cone angle in degrees.")
                            .tooltip()
                            .forGetter(ClientConfig::lightAngle),
                    ConfigField.list("lightColorsARGB", Codec.STRING)
                            .defaultValue(List.of("FFFFFFFF"))
                            .comment("ARGB colors for the beam gradient.")
                            .tooltip()
                            .forGetter(ClientConfig::lightColorsARGB),
                    ConfigField.bool("enableRealLight")
                            .defaultValue(true)
                            .comment("Enable dynamic real light.")
                            .tooltip()
                            .forGetter(ClientConfig::realLight),
                    ConfigField.doubleRange("lightIntensity", 0.0, 1.0)
                            .defaultValue(0.3)
                            .comment("Post effect flashlight intensity.")
                            .tooltip()
                            .forGetter(ClientConfig::lightIntensity),
                    ConfigField.bool("enableLightOcclusion")
                            .defaultValue(false)
                            .comment("Enable beam occlusion.")
                            .tooltip()
                            .forGetter(ClientConfig::lightOcclusion),
                    ConfigField.bool("enableConeRaycast")
                            .defaultValue(false)
                            .comment("Enable cone raycast clipping.")
                            .tooltip()
                            .forGetter(ClientConfig::coneRaycast),
                    ConfigField.doubleRange("realLightLuminance", 0.0, 15.0)
                            .defaultValue(15.0)
                            .comment("Real light luminance level.")
                            .tooltip()
                            .forGetter(ClientConfig::realLightLuminance),
                    ConfigField.list("layerSizeScales", Codec.STRING)
                            .defaultValue(List.of("1.00", "1.08", "1.16"))
                            .comment("Per-layer scale values.")
                            .tooltip()
                            .forGetter(ClientConfig::layerSizeScales),
                    ConfigField.list("layerCenterAlphas", Codec.STRING)
                            .defaultValue(List.of("0.15", "0.12", "0.08"))
                            .comment("Per-layer center alpha values.")
                            .tooltip()
                            .forGetter(ClientConfig::layerCenterAlphas),
                    ConfigField.list("layerEdgeAlphas", Codec.STRING)
                            .defaultValue(List.of("0.00", "0.00", "0.00"))
                            .comment("Per-layer edge alpha values.")
                            .tooltip()
                            .forGetter(ClientConfig::layerEdgeAlphas),
                    ConfigField.list("layerColorsARGB", Codec.STRING)
                            .defaultValue(List.of())
                            .comment("Optional per-layer ARGB colors.")
                            .tooltip()
                            .forGetter(ClientConfig::layerColorsARGB),
                    ConfigField.doubleRange("colorNoiseAmplitude", 0.0, 1.0)
                            .defaultValue(0.35)
                            .comment("Color noise amplitude.")
                            .tooltip()
                            .forGetter(ClientConfig::colorNoiseAmplitude),
                    fogCodec().fieldOf("fog")
                            .orElse(defaultFog())
                            .forGetter(ClientConfig::fog)
            ).apply(instance, ClientConfig::new),
            meta -> meta
                    .directory(HandheldMoon.MOD_ID)
                    .fileName(HandheldMoon.MOD_ID + "_config")
                    .format(ConfigStorageFormat.TOML)
    );

    private static final ConfigAccess<ClientConfig> ACCESS = new ConfigAccess<>(UNIT);

    public static final Value<Boolean> ENABLE_FIXED_FLASHLIGHT = new Value<>(
            () -> UNIT.get().enableFixedFlashlight(),
            value -> ACCESS.set(ClientConfig::enableFixedFlashlight, value)
    );
    public static final Value<Boolean> PLAYER_RAY = new Value<>(
            () -> UNIT.get().playerRay(),
            value -> ACCESS.set(ClientConfig::playerRay, value)
    );
    public static final Value<Double> LIGHT_RANGE = new Value<>(
            () -> UNIT.get().lightRange(),
            value -> ACCESS.set(ClientConfig::lightRange, value)
    );
    public static final Value<Double> LIGHT_ANGLE = new Value<>(
            () -> UNIT.get().lightAngle(),
            value -> ACCESS.set(ClientConfig::lightAngle, value)
    );
    public static final Value<List<String>> LIGHT_COLORS_ARGB = new Value<>(
            () -> UNIT.get().lightColorsARGB(),
            value -> ACCESS.set(ClientConfig::lightColorsARGB, value)
    );
    public static final Value<Boolean> REAL_LIGHT = new Value<>(
            () -> UNIT.get().realLight(),
            value -> ACCESS.set(ClientConfig::realLight, value)
    );
    public static final Value<Double> LIGHT_INTENSITY = new Value<>(
            () -> UNIT.get().lightIntensity(),
            value -> ACCESS.set(ClientConfig::lightIntensity, value)
    );
    public static final Value<Boolean> LIGHT_OCCLUSION = new Value<>(
            () -> UNIT.get().lightOcclusion(),
            value -> ACCESS.set(ClientConfig::lightOcclusion, value)
    );
    public static final Value<Boolean> CONE_RAYCAST = new Value<>(
            () -> UNIT.get().coneRaycast(),
            value -> ACCESS.set(ClientConfig::coneRaycast, value)
    );
    public static final Value<Double> REAL_LIGHT_LUMINANCE = new Value<>(
            () -> UNIT.get().realLightLuminance(),
            value -> ACCESS.set(ClientConfig::realLightLuminance, value)
    );
    public static final Value<List<String>> LAYER_SIZE_SCALES = new Value<>(
            () -> UNIT.get().layerSizeScales(),
            value -> ACCESS.set(ClientConfig::layerSizeScales, value)
    );
    public static final Value<List<String>> LAYER_CENTER_ALPHAS = new Value<>(
            () -> UNIT.get().layerCenterAlphas(),
            value -> ACCESS.set(ClientConfig::layerCenterAlphas, value)
    );
    public static final Value<List<String>> LAYER_EDGE_ALPHAS = new Value<>(
            () -> UNIT.get().layerEdgeAlphas(),
            value -> ACCESS.set(ClientConfig::layerEdgeAlphas, value)
    );
    public static final Value<List<String>> LAYER_COLORS_ARGB = new Value<>(
            () -> UNIT.get().layerColorsARGB(),
            value -> ACCESS.set(ClientConfig::layerColorsARGB, value)
    );
    public static final Value<Double> COLOR_NOISE_AMPLITUDE = new Value<>(
            () -> UNIT.get().colorNoiseAmplitude(),
            value -> ACCESS.set(ClientConfig::colorNoiseAmplitude, value)
    );
    public static final Value<Boolean> FOG_ENABLED = new Value<>(
            () -> UNIT.get().fog().enabled(),
            value -> updateFog(fog -> new FogConfig(
                    value,
                    fog.sizeScale(),
                    fog.centerAlpha(),
                    fog.edgeAlpha(),
                    fog.colorARGB()
            ))
    );
    public static final Value<Double> FOG_SIZE_SCALE = new Value<>(
            () -> UNIT.get().fog().sizeScale(),
            value -> updateFog(fog -> new FogConfig(
                    fog.enabled(),
                    value,
                    fog.centerAlpha(),
                    fog.edgeAlpha(),
                    fog.colorARGB()
            ))
    );
    public static final Value<Double> FOG_CENTER_ALPHA = new Value<>(
            () -> UNIT.get().fog().centerAlpha(),
            value -> updateFog(fog -> new FogConfig(
                    fog.enabled(),
                    fog.sizeScale(),
                    value,
                    fog.edgeAlpha(),
                    fog.colorARGB()
            ))
    );
    public static final Value<Double> FOG_EDGE_ALPHA = new Value<>(
            () -> UNIT.get().fog().edgeAlpha(),
            value -> updateFog(fog -> new FogConfig(
                    fog.enabled(),
                    fog.sizeScale(),
                    fog.centerAlpha(),
                    value,
                    fog.colorARGB()
            ))
    );
    public static final Value<String> FOG_COLOR_ARGB = new Value<>(
            () -> UNIT.get().fog().colorARGB(),
            value -> updateFog(fog -> new FogConfig(
                    fog.enabled(),
                    fog.sizeScale(),
                    fog.centerAlpha(),
                    fog.edgeAlpha(),
                    value
            ))
    );

    public static void register() {
        ConfigManager.registerClient(UNIT);
    }

    private static void updateFog(UnaryOperator<FogConfig> updater) {
        ACCESS.set(ClientConfig::fog, updater.apply(UNIT.get().fog()));
    }

    public static final class Value<T> {
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        private Value(Supplier<T> getter, Consumer<T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public T get() {
            return getter.get();
        }

        public void set(T value) {
            setter.accept(value);
        }

        public void save() {
            UNIT.save();
        }
    }
}
