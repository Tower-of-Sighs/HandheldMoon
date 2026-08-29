package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeBuilder;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
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
        double range = Config.LIGHT_RANGE.get();
        double angle = Config.LIGHT_ANGLE.get();
        List<? extends String> colors = Config.LIGHT_COLORS_ARGB.get();
        List<? extends String> sizeScales = Config.LAYER_SIZE_SCALES.get();
        List<? extends String> centerAlphas = Config.LAYER_CENTER_ALPHAS.get();
        List<? extends String> edgeAlphas = Config.LAYER_EDGE_ALPHAS.get();
        List<? extends String> layerColors = Config.LAYER_COLORS_ARGB.get();
        double noiseAmplitude = Config.COLOR_NOISE_AMPLITUDE.get();
        boolean coneRaycast = Config.CONE_RAYCAST.get();
        boolean fogEnabled = Config.FOG_ENABLED.get();
        double fogSizeScale = Config.FOG_SIZE_SCALE.get();
        double fogCenterAlpha = Config.FOG_CENTER_ALPHA.get();
        double fogEdgeAlpha = Config.FOG_EDGE_ALPHA.get();
        String fogColor = Config.FOG_COLOR_ARGB.get();

        if (GLOBAL_CONFIG_CACHE.matches(
                range, angle, colors, sizeScales, centerAlphas, edgeAlphas,
                layerColors, noiseAmplitude, coneRaycast, fogEnabled,
                fogSizeScale, fogCenterAlpha, fogEdgeAlpha, fogColor
        )) {
            return GLOBAL_CONFIG_CACHE.value;
        }

        RayConeBuilder builder = RayConeBuilder.create()
                .range(range)
                .angle(angle)
                .colorStops(ColorUtils.parseColorStops(colors));

        // layers
        int count = Math.min(sizeScales.size(), Math.min(centerAlphas.size(), edgeAlphas.size()));
        for (int i = 0; i < count; i++) {
            float ss = parseFloat(sizeScales.get(i), 1.0f);
            float ca = clamp01(parseFloat(centerAlphas.get(i), 0.12f));
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
                range, angle, colors, sizeScales, centerAlphas, edgeAlphas,
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
        private List<? extends String> centerAlphas;
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
                List<? extends String> sizeScales, List<? extends String> centerAlphas,
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
                    && this.centerAlphas == centerAlphas
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
                List<? extends String> sizeScales, List<? extends String> centerAlphas,
                List<? extends String> edgeAlphas, List<? extends String> layerColors,
                double noiseAmplitude, boolean coneRaycast, boolean fogEnabled,
                double fogSizeScale, double fogCenterAlpha, double fogEdgeAlpha,
                String fogColor, IRayConeConfig value
        ) {
            this.range = range;
            this.angle = angle;
            this.colors = colors;
            this.sizeScales = sizeScales;
            this.centerAlphas = centerAlphas;
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
     * Colour, layer, noise, raycast and fog settings remain device settings.
     *
     * @param cfg base lamp visual configuration
     * @param profile optional entity profile; when absent, device geometry is used
     * @return immutable cone rendering configuration
     */
    public static IRayConeConfig buildLampConeConfig(
            LampDeviceConfig cfg, EntityLightProfile profile
    ) {
        double range = profile != null ? profile.range() : cfg.lightRange();
        double angle = profile != null
                ? Math.toDegrees(profile.outerAngle() * 2.0)
                : cfg.lightAngle();
        RayConeBuilder builder = RayConeBuilder.create()
                .range(range)
                .angle(angle)
                .colorStops(ColorUtils.parseColorStops(cfg.lightColorsARGB()));

        int count = Math.min(cfg.layerSizeScales().size(),
                Math.min(cfg.layerCenterAlphas().size(), cfg.layerEdgeAlphas().size()));
        for (int i = 0; i < count; i++) {
            float ss = parseFloat(cfg.layerSizeScales().get(i), 1.0f);
            float ca = clamp01(parseFloat(cfg.layerCenterAlphas().get(i), 0.12f));
            float ea = clamp01(parseFloat(cfg.layerEdgeAlphas().get(i), 0.02f));
            float[] lc = null;
            if (i < cfg.layerColorsARGB().size()) {
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
