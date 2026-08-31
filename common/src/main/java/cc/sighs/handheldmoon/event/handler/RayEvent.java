package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeBuilder;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.ColorUtils;
import cc.sighs.handheldmoon.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles per-frame cone rendering for player flashlights and placed lamps.
 * <p>
 * This class remains as a thin adapter that builds {@link IRayConeConfig}
 * from mod configuration and collects {@link RayConeRenderer.ConeSource}s,
 * then delegates to {@link RayConeRenderer#render(PoseStack, Vec3, Matrix4fc, List)}.
 * <p>
 * For external mods wanting custom ray cone rendering, use the API classes
 * directly instead of this handler.
 */
public final class RayEvent {
    private static final int ENTITY_CONE_LAYER_COUNT = 3;
    /**
     * The visible cone renders this many degrees narrower than the real-light
     * cone so the beam stays visually tighter than its lighting footprint.
     */
    private static final double VISIBLE_CONE_ANGLE_OFFSET = 10.0;
    /** Lower bound for the visible cone full angle in degrees. */
    private static final double MIN_CONE_ANGLE = 1.0;
    /**
     * The visible cone extends to this fraction of the real-light range,
     * keeping the beam visually shorter than its lighting footprint.
     */
    private static final double VISIBLE_CONE_RANGE_SCALE = 0.5;
    private static final float[] DEFAULT_LAYER_SIZE_SCALES = {1.00f, 1.08f, 1.16f};
    /**
     * Center-alpha falloff per layer, derived from the legacy baseline
     * {@code 0.15 / 0.12 / 0.08}: the inner layer uses the incoming color
     * alpha directly, the middle layer keeps the {@code 0.12 / 0.15} ratio,
     * and the outer layer keeps the cumulative {@code 0.08 / 0.15} ratio.
     */
    private static final float[] LAYER_CENTER_ALPHA_SCALES = {1.0f, 0.8f, 0.5333334f};
    private static final float[] DEFAULT_LAYER_EDGE_ALPHAS = {0.00f, 0.00f, 0.00f};
    private static final Map<UUID, Vec3> LAST_DIR = new HashMap<>();
    private static final GlobalConeConfigCache GLOBAL_CONFIG_CACHE = new GlobalConeConfigCache();

    private RayEvent() {
    }

    /**
     * Entry point called from platform-specific level render hooks.
     */
    public static void renderPlayerViewConesWithRadialGradient(
            PoseStack poseStack, Vec3 cameraPos,
            Matrix4fc modelViewMatrix, float partialTick
    ) {
        if (!Config.PLAYER_RAY.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<RayConeRenderer.ConeSource> sources = new ArrayList<>();
        IRayConeConfig globalConfig = buildGlobalConeConfig();

        // ---- player cones ----
        for (Player player : mc.level.players()) {
            if (player.getUUID().equals(mc.player.getUUID())) continue;
            if (!Utils.isUsingFlashlight(player)) continue;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 rawDir = player.getViewVector(partialTick).normalize();
            Vec3 prevDir = LAST_DIR.getOrDefault(player.getUUID(), rawDir);
            Vec3 smoothedDir = prevDir.scale(0.7).add(rawDir.scale(0.3)).normalize();
            LAST_DIR.put(player.getUUID(), smoothedDir);

            sources.add(new RayConeRenderer.ConeSource(
                    eyePos, smoothedDir, globalConfig
            ));
        }

        LampConeSourceHooks.append(mc, sources);

        RayConeRenderer.render(poseStack, cameraPos, modelViewMatrix, sources);
    }

    private static IRayConeConfig buildGlobalConeConfig() {
        LampDeviceConfig cfg = LampDeviceConfig.fromGlobalConfig();
        double range = DynamicLightDefaults.FLASHLIGHT_RANGE * VISIBLE_CONE_RANGE_SCALE;
        double angle = Math.max(cfg.lightAngle() - VISIBLE_CONE_ANGLE_OFFSET, MIN_CONE_ANGLE);
        List<? extends String> colors = cfg.lightColorsARGB();
        List<? extends String> sizeScales = cfg.layerSizeScales();
        List<? extends String> edgeAlphas = cfg.layerEdgeAlphas();
        List<? extends String> layerColors = cfg.layerColorsARGB();
        double noiseAmplitude = cfg.colorNoiseAmplitude();
        boolean coneRaycast = cfg.coneRaycast();
        boolean fogEnabled = cfg.fog().enabled();
        double fogSizeScale = cfg.fog().sizeScale();
        double fogCenterAlpha = cfg.fog().centerAlpha();
        double fogEdgeAlpha = cfg.fog().edgeAlpha();
        String fogColor = cfg.fog().colorARGB();

        if (GLOBAL_CONFIG_CACHE.matches(
                range, angle, colors, sizeScales, edgeAlphas,
                layerColors, noiseAmplitude, coneRaycast, fogEnabled,
                fogSizeScale, fogCenterAlpha, fogEdgeAlpha, fogColor
        )) {
            return GLOBAL_CONFIG_CACHE.value;
        }

        RayConeBuilder builder = RayConeBuilder.create()
                .range(range)
                .angle(angle)
                .colorStops(ColorUtils.parseColorStops(colors));

        // layers: the inner alpha follows the first color stop's alpha, then
        // falls off through the derived curve; edge alphas stay configured.
        int count = Math.min(sizeScales.size(), edgeAlphas.size());
        float[] centerAlphas = deriveLayerCenterAlphas(
                ColorUtils.parseColorRGBAWithAlpha(
                        colors.isEmpty() ? EntityLightProfile.DEFAULT_LIGHT_COLOR : colors.get(0))[3],
                count);
        for (int i = 0; i < count; i++) {
            float ss = parseFloat(sizeScales.get(i), 1.0f);
            float ca = centerAlphas[i];
            float ea = clamp01(parseFloat(edgeAlphas.get(i), 0.02f));
            float[] lc = null;
            if (i < layerColors.size()) {
                lc = ColorUtils.parseColorARGB(layerColors.get(i));
            }
            builder.addLayer(ss, ca, ea, lc);
        }

        builder.noiseAmplitude(noiseAmplitude).raycast(coneRaycast);

        // fog
        if (fogEnabled) {
            builder.fog()
                    .enabled(true)
                    .sizeScale(fogSizeScale)
                    .centerAlpha(fogCenterAlpha)
                    .edgeAlpha(fogEdgeAlpha)
                    .color(fogColor)
                    .end();
        }

        GLOBAL_CONFIG_CACHE.update(
                range, angle, colors, sizeScales, edgeAlphas,
                layerColors, noiseAmplitude, coneRaycast, fogEnabled,
                fogSizeScale, fogCenterAlpha, fogEdgeAlpha, fogColor,
                builder.build()
        );
        return GLOBAL_CONFIG_CACHE.value;
    }

    private static final class GlobalConeConfigCache {
        private IRayConeConfig value;
        private double range;
        private double angle;
        private List<? extends String> colors;
        private List<? extends String> sizeScales;
        private List<? extends String> edgeAlphas;
        private List<? extends String> layerColors;
        private double noiseAmplitude;
        private boolean coneRaycast;
        private boolean fogEnabled;
        private double fogSizeScale;
        private double fogCenterAlpha;
        private double fogEdgeAlpha;
        private String fogColor;

        private boolean matches(
                double range, double angle, List<? extends String> colors,
                List<? extends String> sizeScales,
                List<? extends String> edgeAlphas, List<? extends String> layerColors,
                double noiseAmplitude, boolean coneRaycast, boolean fogEnabled,
                double fogSizeScale, double fogCenterAlpha, double fogEdgeAlpha,
                String fogColor
        ) {
            return value != null
                    && this.range == range
                    && this.angle == angle
                    && this.colors == colors
                    && this.sizeScales == sizeScales
                    && this.edgeAlphas == edgeAlphas
                    && this.layerColors == layerColors
                    && this.noiseAmplitude == noiseAmplitude
                    && this.coneRaycast == coneRaycast
                    && this.fogEnabled == fogEnabled
                    && this.fogSizeScale == fogSizeScale
                    && this.fogCenterAlpha == fogCenterAlpha
                    && this.fogEdgeAlpha == fogEdgeAlpha
                    && Objects.equals(this.fogColor, fogColor);
        }

        private void update(
                double range, double angle, List<? extends String> colors,
                List<? extends String> sizeScales,
                List<? extends String> edgeAlphas, List<? extends String> layerColors,
                double noiseAmplitude, boolean coneRaycast, boolean fogEnabled,
                double fogSizeScale, double fogCenterAlpha, double fogEdgeAlpha,
                String fogColor, IRayConeConfig value
        ) {
            this.range = range;
            this.angle = angle;
            this.colors = colors;
            this.sizeScales = sizeScales;
            this.edgeAlphas = edgeAlphas;
            this.layerColors = layerColors;
            this.noiseAmplitude = noiseAmplitude;
            this.coneRaycast = coneRaycast;
            this.fogEnabled = fogEnabled;
            this.fogSizeScale = fogSizeScale;
            this.fogCenterAlpha = fogCenterAlpha;
            this.fogEdgeAlpha = fogEdgeAlpha;
            this.fogColor = fogColor;
            this.value = value;
        }
    }

    public static IRayConeConfig buildLampConeConfig(LampDeviceConfig cfg) {
        return buildLampConeConfig(cfg, null);
    }

    /**
     * Builds a lamp cone while allowing an entity profile to override the
     * geometric properties controlled by the shared entity-light API.
     * Colour comes from the entity profile when one is present; layer, noise,
     * raycast and fog settings remain device settings.
     *
     * @param cfg base lamp visual configuration
     * @param profile optional entity profile; when absent, device geometry is used
     * @return immutable cone rendering configuration
     */
    public static IRayConeConfig buildLampConeConfig(
            LampDeviceConfig cfg, EntityLightProfile profile
    ) {
        double range = profile != null
                ? profile.range() * VISIBLE_CONE_RANGE_SCALE
                : DynamicLightDefaults.FLASHLIGHT_RANGE * VISIBLE_CONE_RANGE_SCALE;
        double angle = profile != null
                ? Math.max(Math.toDegrees(profile.outerAngle() * 2.0) - VISIBLE_CONE_ANGLE_OFFSET, MIN_CONE_ANGLE)
                : Math.max(cfg.lightAngle() - VISIBLE_CONE_ANGLE_OFFSET, MIN_CONE_ANGLE);
        float[] profileColor = profile == null
                ? null : ColorUtils.parseColorRGBAWithAlpha(profile.lightColor());
        List<float[]> colorStops = profileColor == null
                ? ColorUtils.parseColorStops(cfg.lightColorsARGB())
                : List.of(new float[]{profileColor[0], profileColor[1], profileColor[2]});
        RayConeBuilder builder = RayConeBuilder.create()
                .range(range)
                .angle(angle)
                .colorStops(colorStops);

        int count = profile == null
                ? Math.min(cfg.layerSizeScales().size(), cfg.layerEdgeAlphas().size())
                : ENTITY_CONE_LAYER_COUNT;
        float colorAlpha = profileColor == null
                ? ColorUtils.parseColorRGBAWithAlpha(
                cfg.lightColorsARGB().isEmpty()
                        ? EntityLightProfile.DEFAULT_LIGHT_COLOR : cfg.lightColorsARGB().get(0))[3]
                : profileColor[3];
        float[] centerAlphas = deriveLayerCenterAlphas(colorAlpha, count);
        float[] edgeAlphas = deriveLayerEdgeAlphas(cfg.layerEdgeAlphas(), colorAlpha, count);
        for (int i = 0; i < count; i++) {
            float ss = parseFloatAt(cfg.layerSizeScales(), i, DEFAULT_LAYER_SIZE_SCALES[i]);
            float ca = centerAlphas[i];
            float ea = edgeAlphas[i];
            float[] lc = profileColor == null
                    ? null : new float[]{profileColor[0], profileColor[1], profileColor[2]};
            if (profileColor == null && i < cfg.layerColorsARGB().size()) {
                lc = ColorUtils.parseColorARGB(cfg.layerColorsARGB().get(i));
            }
            builder.addLayer(ss, ca, ea, lc);
        }

        builder.noiseAmplitude(cfg.colorNoiseAmplitude())
                .raycast(cfg.coneRaycast());

        LampDeviceConfig.FogSettings fog = cfg.fog();
        if (fog.enabled()) {
            builder.fog()
                    .enabled(true)
                    .sizeScale(fog.sizeScale())
                    .centerAlpha(fog.centerAlpha())
                    .edgeAlpha(fog.edgeAlpha())
                    .color(fog.colorARGB())
                    .end();
        }

        return builder.build();
    }

    /**
     * Derives the per-layer center alpha from the incoming color alpha using
     * the curve implied by the legacy baseline {@code 0.15 / 0.12 / 0.08}:
     * the inner layer keeps the color alpha, and each outer layer falls off
     * by the corresponding ratio of that baseline.
     */
    private static float[] deriveLayerCenterAlphas(float colorAlpha, int count) {
        float[] result = new float[count];
        for (int i = 0; i < count; i++) {
            float scale = i < LAYER_CENTER_ALPHA_SCALES.length
                    ? LAYER_CENTER_ALPHA_SCALES[i]
                    : LAYER_CENTER_ALPHA_SCALES[LAYER_CENTER_ALPHA_SCALES.length - 1];
            result[i] = clamp01(colorAlpha * scale);
        }
        return result;
    }

    /**
     * Derives the per-layer edge alpha: the inner layer scales the color alpha
     * by the configured base, and outer layers follow the same proportional
     * decay as the configured edge baseline (defaults all zero).
     */
    private static float[] deriveLayerEdgeAlphas(
            List<String> configured, float colorAlpha, int count
    ) {
        float[] result = new float[count];
        float previousBase = 0.0f;
        float previousValue = 0.0f;
        for (int i = 0; i < count; i++) {
            float fallback = i < DEFAULT_LAYER_EDGE_ALPHAS.length
                    ? DEFAULT_LAYER_EDGE_ALPHAS[i] : previousBase;
            float currentBase = clamp01(parseFloatAt(configured, i, fallback));
            if (i > 0) {
                currentBase = Math.min(currentBase, previousBase);
            }
            if (i == 0) {
                previousValue = clamp01(colorAlpha * currentBase);
            } else if (previousBase > 1.0E-6f) {
                previousValue = clamp01(previousValue * currentBase / previousBase);
            } else {
                previousValue = clamp01(colorAlpha * currentBase);
            }
            result[i] = previousValue;
            previousBase = currentBase;
        }
        return result;
    }

    private static float parseFloatAt(List<String> values, int index, float fallback) {
        return index >= 0 && index < values.size()
                ? parseFloat(values.get(index), fallback)
                : fallback;
    }

    private static float parseFloat(String value, float fallback) {
        try {
            if (value == null) return fallback;
            return Float.parseFloat(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : Math.min(v, 1f);
    }
}
