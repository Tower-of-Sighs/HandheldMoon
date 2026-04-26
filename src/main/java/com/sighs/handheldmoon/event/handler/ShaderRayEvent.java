//package com.sighs.handheldmoon.event.handler;
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import com.mojang.blaze3d.systems.RenderSystem;
//import com.mojang.blaze3d.vertex.*;
//import com.sighs.handheldmoon.HandheldMoon;
//import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
//import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
//import com.sighs.handheldmoon.registry.Config;
//import com.sighs.handheldmoon.util.AeronauticsUtils;
//import com.sighs.handheldmoon.util.ColorUtils;
//import com.sighs.handheldmoon.util.Utils;
//import foundry.veil.api.client.render.VeilRenderSystem;
//import foundry.veil.api.client.render.shader.program.ShaderProgram;
//import net.minecraft.client.Camera;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.core.BlockPos;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//import net.neoforged.api.distmarker.Dist;
//import net.neoforged.bus.api.SubscribeEvent;
//import net.neoforged.fml.common.EventBusSubscriber;
//import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
//import org.joml.Matrix4f;
//import org.joml.Quaterniond;
//import org.joml.Vector3d;
//
//import java.util.*;
//
//@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
//public class ShaderRayEvent {
//    private static final Map<UUID, Vec3> LAST_DIR = new HashMap<>();
//    private static final ResourceLocation BEAM_SHADER = ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "beam_cone");
//    private static final int SEGMENTS = 24;
//    private static final int MAX_PALETTE_COLORS = 4;
//
//    @SubscribeEvent
//    public static void renderPlayerViewConesWithRadialGradient(RenderLevelStageEvent event) {
//        if (!Config.PLAYER_RAY.get()) return;
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
//
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.level == null || mc.player == null) return;
//
//        ShaderProgram shader = getBeamShader();
//        if (shader == null) return;
//
//        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
//        PoseStack poseStack = event.getPoseStack();
//
//        RenderSystem.enableBlend();
//        RenderSystem.enableDepthTest();
//        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
//        RenderSystem.depthMask(false);
//        RenderSystem.disableCull();
//
//        poseStack.pushPose();
//        try {
//            Camera camera = event.getCamera();
//            Vec3 cameraPos = camera.getPosition();
//            poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
//
//            List<AbstractClientPlayer> players = mc.level.players();
//
//            for (Player player : players) {
//                if (player.getUUID().equals(mc.player.getUUID())) continue;
//
//                if (!Utils.isUsingFlashlight(player)) continue;
//
//                Vec3 eyePos = player.getEyePosition(partialTick);
//                Vec3 viewVecRaw = player.getViewVector(partialTick).normalize();
//                Vec3 prev = LAST_DIR.getOrDefault(player.getUUID(), viewVecRaw);
//                Vec3 viewVec = prev.scale(0.7).add(viewVecRaw.scale(0.3)).normalize();
//                LAST_DIR.put(player.getUUID(), viewVec);
//
//                renderCones(shader, poseStack, eyePos, viewVec);
//            }
//
//            for (BlockPos pos : HandheldMoonDynamicLightsInitializer.getActiveLampPositions()) {
//                var be = mc.level.getBlockEntity(pos);
//                if (be instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
//                    Vec3 eyePos = pos.getCenter();
//                    Vec3 viewVec = lamp.getViewVec().normalize().scale(-1);
//                    if (AeronauticsUtils.isPhysicalized(be)) {
//                        eyePos = AeronauticsUtils.getPhysicalizedRenderPosition(be);
//                        Vector3d jomlVec = new Vector3d(viewVec.x, viewVec.y, viewVec.z);
//                        Quaterniond direction = AeronauticsUtils.getPhysicalizedRenderOrientation(be);
//                        if (direction != null) {
//                            direction.transform(jomlVec);
//                            viewVec = new Vec3(jomlVec.x, jomlVec.y, jomlVec.z);
//                        }
//                    }
//                    renderCones(shader, poseStack, eyePos, viewVec);
//                }
//            }
//        } finally {
//            poseStack.popPose();
//            ShaderProgram.unbind();
//            RenderSystem.disableBlend();
//            RenderSystem.disableDepthTest();
//            RenderSystem.depthMask(true);
//            RenderSystem.enableCull();
//            RenderSystem.defaultBlendFunc();
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public static void renderCones(ShaderProgram shader, PoseStack poseStack, Vec3 apex, Vec3 direction) {
//        double range = Config.LIGHT_RANGE.get();
//        double angle = Config.LIGHT_ANGLE.get();
//        List<float[]> stops = ColorUtils.parseColorStops(Config.LIGHT_COLORS_ARGB.get());
//        List<String> sizeStr = (List<String>) Config.LAYER_SIZE_SCALES.get();
//        List<String> centerStr = (List<String>) Config.LAYER_CENTER_ALPHAS.get();
//        List<String> edgeStr = (List<String>) Config.LAYER_EDGE_ALPHAS.get();
//        List<String> layerColorStr = (List<String>) Config.LAYER_COLORS_ARGB.get();
//        int layerCount = Math.min(sizeStr.size(), Math.min(centerStr.size(), edgeStr.size()));
//        double noiseAmp = Config.COLOR_NOISE_AMPLITUDE.get();
//        for (int i = 0; i < layerCount; i++) {
//            float sizeScale = parseFloat(sizeStr.get(i), 1.0f);
//            float centerAlpha = clamp01(parseFloat(centerStr.get(i), 0.12f));
//            float edgeAlpha = clamp01(parseFloat(edgeStr.get(i), 0.02f));
//            float[] layerColor = null;
//            if (i < layerColorStr.size()) {
//                layerColor = ColorUtils.parseColorARGB(layerColorStr.get(i));
//            }
//            renderConeLayer(shader, poseStack, apex, direction, (float) range, (float) angle, stops, sizeScale, centerAlpha, edgeAlpha, layerColor, (float) noiseAmp);
//        }
//        if (Config.FOG_ENABLED.get()) {
//            float[] fog = ColorUtils.parseColorARGB(Config.FOG_COLOR_ARGB.get());
//            List<float[]> fogStops = java.util.List.of(fog);
//            renderConeLayer(shader, poseStack, apex, direction, (float) range, (float) angle, fogStops,
//                    Config.FOG_SIZE_SCALE.get().floatValue(),
//                    Config.FOG_CENTER_ALPHA.get().floatValue(),
//                    Config.FOG_EDGE_ALPHA.get().floatValue(),
//                    fog,
//                    0.0f);
//        }
//    }
//
//    public static void renderConeLayer(
//            ShaderProgram shader,
//            PoseStack poseStack,
//            Vec3 apex,
//            Vec3 direction,
//            float baseRange,
//            float baseAngleDeg,
//            List<float[]> colorStops,
//            float sizeScale,
//            float centerAlpha,
//            float edgeAlpha,
//            float[] layerColorOverride,
//            float noiseAmplitude
//    ) {
//        float scaledRange = baseRange * sizeScale;
//        float scaledHalfAngleRad = (float) Math.toRadians(baseAngleDeg * sizeScale / 2.0f);
//        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);
//
//        Vec3 baseCenter = apex.add(direction.scale(scaledRange));
//
//        Vec3 upReference = new Vec3(0, 1, 0);
//        Vec3 rightVec, orthoUp;
//        if (Math.abs(direction.dot(upReference)) > 0.99) {
//            upReference = new Vec3(0, 0, 1);
//        }
//        rightVec = upReference.cross(direction).normalize();
//        orthoUp = direction.cross(rightVec).normalize();
//
//        Tesselator tess = Tesselator.getInstance();
//        BufferBuilder buffer = tess.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX_COLOR);
//        Matrix4f matrix = poseStack.last().pose();
//
//        shader.bind();
//        shader.setDefaultUniforms(VertexFormat.Mode.TRIANGLE_FAN);
//        configureShader(shader, colorStops, centerAlpha, edgeAlpha, layerColorOverride, noiseAmplitude);
//
//        buffer.addVertex(matrix, (float) apex.x, (float) apex.y, (float) apex.z)
//                .setUv(0.0f, 0.0f)
//                .setColor(1.0f, 1.0f, 1.0f, 1.0f);
//
//        for (int i = 0; i <= SEGMENTS; i++) {
//            double theta = 2.0 * Math.PI * i / SEGMENTS;
//            double cos = Math.cos(theta);
//            double sin = Math.sin(theta);
//            Vec3 basePoint = baseCenter
//                    .add(rightVec.scale(scaledRadius * cos))
//                    .add(orthoUp.scale(scaledRadius * sin));
//
//            float thetaNorm = (float) (i / (double) SEGMENTS);
//            buffer.addVertex(matrix, (float) basePoint.x, (float) basePoint.y, (float) basePoint.z)
//                    .setUv(1.0f, thetaNorm)
//                    .setColor(1.0f, 1.0f, 1.0f, 1.0f);
//        }
//
//        BufferUploader.draw(buffer.buildOrThrow());
//    }
//
//    private static ShaderProgram getBeamShader() {
//        if (VeilRenderSystem.renderer() == null) return null;
//        ShaderProgram shader = VeilRenderSystem.renderer().getShaderManager().getShader(BEAM_SHADER);
//        return shader != null && shader.isValid() ? shader : null;
//    }
//
//    private static void configureShader(
//            ShaderProgram shader,
//            List<float[]> colorStops,
//            float centerAlpha,
//            float edgeAlpha,
//            float[] layerColorOverride,
//            float noiseAmplitude
//    ) {
//        setFloat(shader, "uCenterAlpha", centerAlpha);
//        setFloat(shader, "uEdgeAlpha", edgeAlpha);
//        setFloat(shader, "uNoiseAmp", noiseAmplitude);
//        setFloat(shader, "uUseOverride", layerColorOverride != null ? 1.0f : 0.0f);
//
//        float[] override = layerColorOverride != null ? layerColorOverride : new float[]{1.0f, 1.0f, 1.0f};
//        setVector(shader, "uOverrideColor", override);
//
//        int count = Math.min(MAX_PALETTE_COLORS, colorStops.size());
//        setFloat(shader, "uPalCount", count);
//        for (int i = 0; i < MAX_PALETTE_COLORS; i++) {
//            float[] color = i < count ? colorStops.get(i) : colorStops.get(Math.max(0, count - 1));
//            setVector(shader, "uPal" + i, color);
//        }
//    }
//
//    private static void setFloat(ShaderProgram shader, String name, float value) {
//        var uniform = shader.getUniform(name);
//        if (uniform != null) {
//            uniform.setFloat(value);
//        }
//    }
//
//    private static void setVector(ShaderProgram shader, String name, float[] color) {
//        var uniform = shader.getUniform(name);
//        if (uniform != null) {
//            uniform.setVector(color[0], color[1], color[2]);
//        }
//    }
//
//    private static float parseFloat(String s, float fallback) {
//        try {
//            if (s == null) return fallback;
//            return Float.parseFloat(s.trim());
//        } catch (Exception e) {
//            return fallback;
//        }
//    }
//
//    private static float clamp01(float v) {
//        if (v < 0f) return 0f;
//        return Math.min(v, 1f);
//    }
//}