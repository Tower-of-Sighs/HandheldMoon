package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.AeronauticsUtils;
import com.sighs.handheldmoon.util.ColorUtils;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.*;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class RayEvent {
    private static final Map<UUID, Vec3> LAST_DIR = new HashMap<>();
    private static final int SEGMENTS = 32;

    @SubscribeEvent
    public static void renderPlayerViewConesWithRadialGradient(RenderLevelStageEvent event) {
        if (!Config.PLAYER_RAY.get()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        PoseStack poseStack = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();

        poseStack.pushPose();

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        List<AbstractClientPlayer> players = mc.level.players();

        for (Player player : players) {
            if (player.getUUID().equals(mc.player.getUUID())) continue;

            if (!Utils.isUsingFlashlight(player)) continue;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 viewVecRaw = player.getViewVector(partialTick).normalize();
            Vec3 prev = LAST_DIR.getOrDefault(player.getUUID(), viewVecRaw);
            Vec3 viewVec = prev.scale(0.7).add(viewVecRaw.scale(0.3)).normalize();
            LAST_DIR.put(player.getUUID(), viewVec);

            renderCones(poseStack, eyePos, viewVec);
        }

        for (BlockPos pos : HandheldMoonDynamicLightsInitializer.getActiveLampPositions()) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
                Vec3 eyePos = pos.getCenter();
                Vec3 viewVec = lamp.getViewVec().normalize().scale(-1);
                if (AeronauticsUtils.isPhysicalized(be)) {
                    eyePos = AeronauticsUtils.getPhysicalizedRenderPosition(be);
                    Vector3d jomlVec = new Vector3d(viewVec.x, viewVec.y, viewVec.z);
                    Quaterniond direction = AeronauticsUtils.getPhysicalizedRenderOrientation(be);
                    if (direction != null) {
                        direction.transform(jomlVec);
                        viewVec = new Vec3(jomlVec.x, jomlVec.y, jomlVec.z);
                    }
                }
                renderCones(poseStack, eyePos, viewVec);
            }
        }

        poseStack.popPose();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    @SuppressWarnings("unchecked")
    public static void renderCones(PoseStack poseStack, Vec3 apex, Vec3 direction) {
        double range = Config.LIGHT_RANGE.get();
        double angle = Config.LIGHT_ANGLE.get();
        List<float[]> stops = ColorUtils.parseColorStops(Config.LIGHT_COLORS_ARGB.get());
        List<String> sizeStr = (List<String>) Config.LAYER_SIZE_SCALES.get();
        List<String> centerStr = (List<String>) Config.LAYER_CENTER_ALPHAS.get();
        List<String> edgeStr = (List<String>) Config.LAYER_EDGE_ALPHAS.get();
        List<String> layerColorStr = (List<String>) Config.LAYER_COLORS_ARGB.get();
        int layerCount = Math.min(sizeStr.size(), Math.min(centerStr.size(), edgeStr.size()));
        double noiseAmp = Config.COLOR_NOISE_AMPLITUDE.get();
        for (int i = 0; i < layerCount; i++) {
            float sizeScale = parseFloat(sizeStr.get(i), 1.0f);
            float centerAlpha = clamp01(parseFloat(centerStr.get(i), 0.12f));
            float edgeAlpha = clamp01(parseFloat(edgeStr.get(i), 0.02f));
            float[] layerColor = null;
            if (i < layerColorStr.size()) {
                layerColor = ColorUtils.parseColorARGB(layerColorStr.get(i));
            }
            renderConeLayer(poseStack, apex, direction, (float) range, (float) angle, stops, sizeScale, centerAlpha, edgeAlpha, layerColor, (float) noiseAmp);
        }
        if (Config.FOG_ENABLED.get()) {
            float[] fog = ColorUtils.parseColorARGB(Config.FOG_COLOR_ARGB.get());
            List<float[]> fogStops = java.util.List.of(fog);
            renderConeLayer(poseStack, apex, direction, (float) range, (float) angle, fogStops,
                    Config.FOG_SIZE_SCALE.get().floatValue(),
                    Config.FOG_CENTER_ALPHA.get().floatValue(),
                    Config.FOG_EDGE_ALPHA.get().floatValue(),
                    fog,
                    0.0f);
        }
    }

    public static void renderConeLayer(
            PoseStack poseStack,
            Vec3 apex,
            Vec3 direction,
            float baseRange,
            float baseAngleDeg,
            List<float[]> colorStops,
            float sizeScale,
            float centerAlpha,
            float edgeAlpha,
            float[] layerColorOverride,
            float noiseAmplitude
    ) {
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngleRad = (float) Math.toRadians(baseAngleDeg * sizeScale / 2.0f);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);

        Vec3 baseCenter = apex.add(direction.scale(scaledRange));

        Vec3 upReference = new Vec3(0, 1, 0);
        Vec3 rightVec, orthoUp;
        if (Math.abs(direction.dot(upReference)) > 0.99) {
            upReference = new Vec3(0, 0, 1);
        }
        rightVec = upReference.cross(direction).normalize();
        orthoUp = direction.cross(rightVec).normalize();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        float[] cCenter = layerColorOverride != null ? layerColorOverride : ColorUtils.colorAt(colorStops, 0.0f);
        buffer.addVertex(matrix, (float) apex.x, (float) apex.y, (float) apex.z)
                .setColor(cCenter[0], cCenter[1], cCenter[2], centerAlpha);

        long seed = Double.doubleToLongBits(apex.x) ^ Double.doubleToLongBits(apex.y) ^ Double.doubleToLongBits(apex.z)
                ^ Double.doubleToLongBits(direction.x) ^ Double.doubleToLongBits(direction.y) ^ Double.doubleToLongBits(direction.z);

        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = 2.0 * Math.PI * i / SEGMENTS;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);
            Vec3 basePoint = baseCenter
                    .add(rightVec.scale(scaledRadius * cos))
                    .add(orthoUp.scale(scaledRadius * sin));

            if (Config.CONE_RAYCAST.get()) {
                HitResult hit = Minecraft.getInstance().level.clip(new ClipContext(
                        apex, basePoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()
                ));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    basePoint = hit.getLocation();
                }
            }

            float thetaNorm = (float) (i / (double) SEGMENTS);
            float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
            float[] cEdge = ColorUtils.colorAtWithNoise(colorStops, baseT, thetaNorm, seed, noiseAmplitude);
            float alphaLocal = edgeAlpha * (0.85f + 0.15f * ((float) Math.sin(thetaNorm * 11.0 + seed * 0.001) * 0.5f + 0.5f));
            buffer.addVertex(matrix, (float) basePoint.x, (float) basePoint.y, (float) basePoint.z)
                    .setColor(cEdge[0], cEdge[1], cEdge[2], alphaLocal);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float parseFloat(String s, float fallback) {
        try {
            if (s == null) return fallback;
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static float clamp01(float v) {
        if (v < 0f) return 0f;
        return Math.min(v, 1f);
    }
}
