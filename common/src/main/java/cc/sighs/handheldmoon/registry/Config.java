package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;

import java.util.List;

/** Shared configuration values used by the common runtime. */
public final class Config {
    public static final Value<Boolean> ENABLE_FIXED_FLASHLIGHT = value(false);
    public static final Value<Boolean> PLAYER_RAY = value(true);
    public static final Value<Double> LIGHT_RANGE = value(14.0);
    public static final Value<Double> LIGHT_ANGLE = value(56.0);
    public static final Value<List<String>> LIGHT_COLORS_ARGB = value(List.of("FFFFFFFF"));
    public static final Value<Boolean> REAL_LIGHT = value(true);
    public static final Value<Double> LIGHT_INTENSITY = value(0.3);
    public static final Value<Boolean> LIGHT_OCCLUSION = value(false);
    public static final Value<Boolean> CONE_RAYCAST = value(false);
    public static final Value<Double> REAL_LIGHT_LUMINANCE = value(15.0);
    public static final Value<List<String>> LAYER_SIZE_SCALES = value(List.of("1.00", "1.08", "1.16"));
    public static final Value<List<String>> LAYER_CENTER_ALPHAS = value(List.of("0.15", "0.12", "0.08"));
    public static final Value<List<String>> LAYER_EDGE_ALPHAS = value(List.of("0.00", "0.00", "0.00"));
    public static final Value<List<String>> LAYER_COLORS_ARGB = value(List.of());
    public static final Value<Double> COLOR_NOISE_AMPLITUDE = value(0.35);
    public static final Value<Boolean> FOG_ENABLED = value(false);
    public static final Value<Double> FOG_SIZE_SCALE = value(1.30);
    public static final Value<Double> FOG_CENTER_ALPHA = value(0.06);
    public static final Value<Double> FOG_EDGE_ALPHA = value(0.05);
    public static final Value<String> FOG_COLOR_ARGB = value("80FFFFFF");

    private Config() {
    }

    public static void register() {
        LampDeviceConfig.setGlobalConfigSupplier(() -> new LampDeviceConfig(
                LIGHT_RANGE.get(), LIGHT_ANGLE.get(), List.copyOf(LIGHT_COLORS_ARGB.get()), REAL_LIGHT.get(),
                LIGHT_INTENSITY.get(), LIGHT_OCCLUSION.get(), CONE_RAYCAST.get(), REAL_LIGHT_LUMINANCE.get(),
                List.copyOf(LAYER_SIZE_SCALES.get()), List.copyOf(LAYER_CENTER_ALPHAS.get()),
                List.copyOf(LAYER_EDGE_ALPHAS.get()), List.copyOf(LAYER_COLORS_ARGB.get()), COLOR_NOISE_AMPLITUDE.get(),
                new LampDeviceConfig.FogSettings(FOG_ENABLED.get(), FOG_SIZE_SCALE.get(), FOG_CENTER_ALPHA.get(),
                        FOG_EDGE_ALPHA.get(), FOG_COLOR_ARGB.get())));
        FullMoonDeviceConfig.setGlobalConfigSupplier(() -> new FullMoonDeviceConfig(
                REAL_LIGHT.get(), REAL_LIGHT_LUMINANCE.get(), LIGHT_OCCLUSION.get()));
    }

    private static <T> Value<T> value(T initial) {
        return new Value<>(initial);
    }

    public static final class Value<T> {
        private T current;

        private Value(T initial) {
            current = initial;
        }

        public T get() {
            return current;
        }

        public void set(T value) {
            current = value;
        }

        public void save() {
            // Loader integrations may persist values; common code only owns the model.
        }
    }
}
