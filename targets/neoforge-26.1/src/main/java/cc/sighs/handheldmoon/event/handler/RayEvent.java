package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.LampConeTransformHooks;
import cc.sighs.handheldmoon.api.raycone.RayConeBuilder;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.ColorUtils;
import cc.sighs.handheldmoon.util.Utils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    eyePos, smoothedDir, buildGlobalConeConfig()
            ));
        }

        // ---- lamp cones ----
        for (BlockPos pos : HandheldMoonDynamicLightsInitializer.getActiveLampPositions()) {
            var blockEntity = mc.level.getBlockEntity(pos);
            if (blockEntity instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
                LampDeviceConfig cfg = lamp.getLampConfig();
                Vec3 dir = lamp.getViewVec().normalize().scale(-1);
                Vec3 apex = pos.getCenter().add(dir.scale(0.24));
                LampConeTransformHooks.LampCone transformed = LampConeTransformHooks.transform(lamp, apex, dir);
                sources.add(new RayConeRenderer.ConeSource(
                        transformed.apex(), transformed.direction(), buildLampConeConfig(cfg), true
                ));
            }
        }

        RayConeRenderer.render(poseStack, cameraPos, modelViewMatrix, sources);
    }

    private static IRayConeConfig buildGlobalConeConfig() {
        RayConeBuilder builder = RayConeBuilder.create()
                .range(Config.LIGHT_RANGE.get())
                .angle(Config.LIGHT_ANGLE.get())
                .colorStops(ColorUtils.parseColorStops(Config.LIGHT_COLORS_ARGB.get()));

        // layers
        List<? extends String> sizeScales = Config.LAYER_SIZE_SCALES.get();
        List<? extends String> centerAlphas = Config.LAYER_CENTER_ALPHAS.get();
        List<? extends String> edgeAlphas = Config.LAYER_EDGE_ALPHAS.get();
        List<? extends String> layerColors = Config.LAYER_COLORS_ARGB.get();
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

        builder.noiseAmplitude(Config.COLOR_NOISE_AMPLITUDE.get())
                .raycast(Config.CONE_RAYCAST.get());

        // fog
        if (Config.FOG_ENABLED.get()) {
            builder.fog()
                    .enabled(true)
                    .sizeScale(Config.FOG_SIZE_SCALE.get())
                    .centerAlpha(Config.FOG_CENTER_ALPHA.get())
                    .edgeAlpha(Config.FOG_EDGE_ALPHA.get())
                    .color(Config.FOG_COLOR_ARGB.get())
                    .end();
        }

        return builder.build();
    }

    private static IRayConeConfig buildLampConeConfig(LampDeviceConfig cfg) {
        RayConeBuilder builder = RayConeBuilder.create()
                .range(cfg.lightRange())
                .angle(cfg.lightAngle())
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
