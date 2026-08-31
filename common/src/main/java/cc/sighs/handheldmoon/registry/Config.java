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

    /** Default for {@link #REAL_LIGHT}; mirrors {@code LampDeviceConfig.builtInDefaults().realLight()}. */
    public static final boolean DEFAULT_REAL_LIGHT = true;
    /** Default for {@link #LIGHT_INTENSITY}; mirrors {@code LampDeviceConfig.builtInDefaults().lightIntensity()}. */
    public static final double DEFAULT_LIGHT_INTENSITY = 0.3;
    /** Default for {@link #CONE_RAYCAST}; mirrors {@code LampDeviceConfig.builtInDefaults().coneRaycast()}. */
    public static final boolean DEFAULT_CONE_RAYCAST = false;

    public static final Value<Boolean> ENABLE_FIXED_FLASHLIGHT = value("enableFixedFlashlight", false);
    public static final Value<Boolean> PLAYER_RAY = value("enablePlayerRay", true);
    public static final Value<Boolean> REAL_LIGHT = value("enableRealLight", DEFAULT_REAL_LIGHT);
    public static final Value<Double> LIGHT_INTENSITY = value("lightIntensity", DEFAULT_LIGHT_INTENSITY);
    public static final Value<Boolean> CONE_RAYCAST = value("enableConeRaycast", DEFAULT_CONE_RAYCAST);

    private Config() {
    }

    public static void register() {
        LampDeviceConfig.setGlobalConfigSupplier(() -> LampDeviceConfig.builtInDefaults()
                .withRealLight(REAL_LIGHT.get())
                .withLightIntensity(LIGHT_INTENSITY.get())
                .withConeRaycast(CONE_RAYCAST.get()));
        FullMoonDeviceConfig.setGlobalConfigSupplier(() ->
                FullMoonDeviceConfig.builtInDefaults().withRealLight(REAL_LIGHT.get()));
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
        REAL_LIGHT.set(parseBoolean(props, REAL_LIGHT.key, REAL_LIGHT.get()));
        LIGHT_INTENSITY.set(parseDouble(props, LIGHT_INTENSITY.key, LIGHT_INTENSITY.get()));
        CONE_RAYCAST.set(parseBoolean(props, CONE_RAYCAST.key, CONE_RAYCAST.get()));
    }

    private static boolean parseBoolean(Properties props, String key, boolean fallback) {
        String value = props.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static double parseDouble(Properties props, String key, double fallback) {
        String value = props.getProperty(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
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
        props.setProperty(REAL_LIGHT.key, Boolean.toString(REAL_LIGHT.get()));
        props.setProperty(LIGHT_INTENSITY.key, Double.toString(LIGHT_INTENSITY.get()));
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
