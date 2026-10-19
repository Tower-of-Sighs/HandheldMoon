package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.spi.PlatformServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Shared configuration values used by the common runtime. */
public final class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    private static final String CONFIG_FILE_NAME = "handheldmoon.properties";

    /** Default for {@link #CONE_RAYCAST}. */
    public static final boolean DEFAULT_CONE_RAYCAST = false;

    public static final Value<Boolean> ENABLE_FIXED_FLASHLIGHT = value("enableFixedFlashlight", false);
    /** Gates the cones rendered for other players only; see {@code LampDeviceConfig.enableVisibleCone()}. */
    public static final Value<Boolean> PLAYER_RAY = value("enablePlayerRay", true);
    public static final Value<Boolean> CONE_RAYCAST = value("enableConeRaycast", DEFAULT_CONE_RAYCAST);

    private Config() {
    }

    public static void register() {
        LampDeviceConfig.setGlobalConfigSupplier(LampDeviceConfig::builtInDefaults);
        FullMoonDeviceConfig.setGlobalConfigSupplier(FullMoonDeviceConfig::builtInDefaults);
    }

    /**
     * Returns the ARGB color of the first configured light color, falling back
     * to opaque white when the list is empty. Used to tint real light sources.
     */
    public static int lightColorArgb() {
        return lightColorArgb(LampDeviceConfig.fromGlobalConfig());
    }

    /**
     * Returns the ARGB light color of the given device config's first entry,
     * falling back to the global config when {@code config} is {@code null}.
     */
    public static int lightColorArgb(LampDeviceConfig config) {
        if (config == null) {
            config = LampDeviceConfig.fromGlobalConfig();
        }
        java.util.List<String> colors = config.lightColorsARGB();
        if (colors == null || colors.isEmpty()) {
            return 0xFFFFFFFF;
        }
        float[] rgba = cc.sighs.handheldmoon.util.ColorUtils.parseColorRGBAWithAlpha(colors.get(0));
        int r = (int) (rgba[0] * 255.0f);
        int g = (int) (rgba[1] * 255.0f);
        int b = (int) (rgba[2] * 255.0f);
        int a = (int) (rgba[3] * 255.0f);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    /**
     * Returns the real-light color of the flashlight. The renderer treats the
     * color's alpha channel as the maximum tint amount, so an installed lens
     * supplies its RGB and maps {@code lensTintStrength} onto alpha. Without a
     * lens the configured base color is returned unchanged.
     */
    public static int flashlightColorArgb(LampDeviceConfig config) {
        int base = lightColorArgb(config);
        if (config == null || !config.hasLens()) {
            return base;
        }
        int alpha = (int) Math.round(255.0 * config.lensTintStrength());
        if (alpha <= 0) {
            return base;
        }
        return (alpha & 0xFF) << 24 | (config.lensColor() & 0xFFFFFF);
    }

    /** Loads persisted values, creating a default config file on first run. */
    public static void load() {
        Path file = configFile();
        if (file == null) {
            return;
        }
        if (!Files.isRegularFile(file)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException e) {
            LOGGER.warn("Failed to read config file {}: {}", file, e.toString());
            return;
        }
        ENABLE_FIXED_FLASHLIGHT.set(parseBoolean(props, ENABLE_FIXED_FLASHLIGHT.key, ENABLE_FIXED_FLASHLIGHT.get()));
        PLAYER_RAY.set(parseBoolean(props, PLAYER_RAY.key, PLAYER_RAY.get()));
        CONE_RAYCAST.set(parseBoolean(props, CONE_RAYCAST.key, CONE_RAYCAST.get()));
        // Drop keys that moved to per-device or per-item configuration.
        if (props.containsKey("enableRealLight")
                || props.containsKey("lightIntensity")
                || props.containsKey("enableVisibleCone")) {
            save();
        }
    }

    private static boolean parseBoolean(Properties props, String key, boolean fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    /** Writes all values to the loader configuration directory. */
    public static void save() {
        Path file = configFile();
        if (file == null) {
            return;
        }
        Properties props = new Properties();
        props.setProperty(ENABLE_FIXED_FLASHLIGHT.key, Boolean.toString(ENABLE_FIXED_FLASHLIGHT.get()));
        props.setProperty(PLAYER_RAY.key, Boolean.toString(PLAYER_RAY.get()));
        props.setProperty(CONE_RAYCAST.key, Boolean.toString(CONE_RAYCAST.get()));
        try {
            Path dir = file.getParent();
            if (dir != null) {
                Files.createDirectories(dir);
            }
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                props.store(writer, "Handheld Moon client settings");
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write config file {}: {}", file, e.toString());
        }
    }

    private static Path configFile() {
        try {
            return PlatformServices.require().configDirectory().resolve(CONFIG_FILE_NAME);
        } catch (RuntimeException e) {
            LOGGER.warn("Config directory unavailable: {}", e.toString());
            return null;
        }
    }

    private static <T> Value<T> value(String key, T initial) {
        return new Value<>(key, initial);
    }

    public static final class Value<T> {
        private final String key;
        private T current;

        private Value(String key, T initial) {
            this.key = key;
            current = initial;
        }

        public T get() {
            return current;
        }

        public void set(T value) {
            current = value;
            LampDeviceConfig.invalidateGlobalConfig();
            FullMoonDeviceConfig.invalidateGlobalConfig();
        }

        public void save() {
            Config.save();
        }
    }
}
