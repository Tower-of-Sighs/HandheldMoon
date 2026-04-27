package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.ColorUtils;
import cc.sighs.handheldmoon.util.IrisCompat;
import cc.sighs.handheldmoon.util.Utils;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RayEvent {
    private static final Map<UUID, Vec3> LAST_DIR = new HashMap<>();
    private static final int SEGMENTS = 24;
    private static final CollisionContext EMPTY_COLLISION = CollisionContext.empty();
    private static final RenderPipeline RAY_CONE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "pipeline/ray_cone"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
            .build();
    private static final RenderType RAY_CONE_RENDER_TYPE = RenderType.create(
            "handheldmoon_ray_cone",
            RenderSetup.builder(RAY_CONE_PIPELINE).sortOnUpload().createRenderSetup()
    );
    private static final RenderPipeline IRIS_RAY_CONE_PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "pipeline/ray_cone_iris"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withSampler("Sampler1")
            .withSampler("Sampler2")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
            .build();
    private static final RenderType IRIS_RAY_CONE_RENDER_TYPE = RenderType.create(
            "handheldmoon_ray_cone_iris",
            RenderSetup.builder(IRIS_RAY_CONE_PIPELINE).useOverlay().useLightmap().sortOnUpload().createRenderSetup()
    );

    private record ConeRenderConfig(
            double range,
            double angle,
            List<float[]> stops,
            List<String> sizeScales,
            List<String> centerAlphas,
            List<String> edgeAlphas,
            List<String> layerColors,
            double noiseAmplitude,
            boolean coneRaycast,
            boolean fogEnabled,
            double fogSizeScale,
            double fogCenterAlpha,
            double fogEdgeAlpha,
            String fogColorARGB
    ) {
    }

    private RayEvent() {
    }

    public static void renderPlayerViewConesWithRadialGradient(PoseStack poseStack, Vec3 cameraPos, Matrix4fc modelViewMatrix, float partialTick) {
        if (!Config.PLAYER_RAY.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        boolean irisShaderPackInUse = IrisCompat.isShaderPackInUse();
        if (irisShaderPackInUse) {
            IrisCompat.assignRayConePipeline(IRIS_RAY_CONE_PIPELINE);
        }

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.set(new Matrix4f(modelViewMatrix)).translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        poseStack.pushPose();

        List<AbstractClientPlayer> players = mc.level.players();
        for (Player player : players) {
            if (player.getUUID().equals(mc.player.getUUID())) {
                continue;
            }
            if (!Utils.isUsingFlashlight(player)) {
                continue;
            }
            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 viewVecRaw = player.getViewVector(partialTick).normalize();
            Vec3 previous = LAST_DIR.getOrDefault(player.getUUID(), viewVecRaw);
            Vec3 viewVec = previous.scale(0.7).add(viewVecRaw.scale(0.3)).normalize();
            LAST_DIR.put(player.getUUID(), viewVec);
            renderCones(poseStack, eyePos, viewVec, buildGlobalConeConfig(), false, irisShaderPackInUse);
        }

        for (BlockPos pos : HandheldMoonDynamicLightsInitializer.getActiveLampPositions()) {
            var blockEntity = mc.level.getBlockEntity(pos);
            if (blockEntity instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
                LampDeviceConfig cfg = lamp.getLampConfig();
                Vec3 viewVec = lamp.getViewVec().normalize().scale(-1);
                // Move apex slightly to the lamp muzzle so raycast clipping is visible for placed lamps too.
                Vec3 eyePos = pos.getCenter().add(viewVec.scale(0.24));
                renderCones(poseStack, eyePos, viewVec, buildLampConeConfig(cfg), true, irisShaderPackInUse);
            }
        }

        poseStack.popPose();

        mvStack.popMatrix();
    }

    private static void renderCones(
            PoseStack poseStack,
            Vec3 apex,
            Vec3 direction,
            ConeRenderConfig config,
            boolean raycastAllLayers,
            boolean irisShaderPackInUse
    ) {
        List<float[]> stops = config.stops();
        List<String> sizeScales = config.sizeScales();
        List<String> centerAlphas = config.centerAlphas();
        List<String> edgeAlphas = config.edgeAlphas();
        List<String> layerColors = config.layerColors();
        int layerCount = Math.min(sizeScales.size(), Math.min(centerAlphas.size(), edgeAlphas.size()));
        double noiseAmplitude = config.noiseAmplitude();

        for (int i = 0; i < layerCount; i++) {
            float sizeScale = parseFloat(sizeScales.get(i), 1.0f);
            float centerAlpha = clamp01(parseFloat(centerAlphas.get(i), 0.12f));
            float edgeAlpha = clamp01(parseFloat(edgeAlphas.get(i), 0.02f));
            float[] layerColor = null;
            if (i < layerColors.size()) {
                layerColor = ColorUtils.parseColorARGB(layerColors.get(i));
            }

            renderConeLayer(
                    poseStack,
                    apex,
                    direction,
                    (float) config.range(),
                    (float) config.angle(),
                    stops,
                    sizeScale,
                    centerAlpha,
                    edgeAlpha,
                    layerColor,
                    (float) noiseAmplitude,
                    config.coneRaycast() && (raycastAllLayers || i == 0),
                    irisShaderPackInUse
            );
        }

        if (config.fogEnabled()) {
            float[] fogColor = ColorUtils.parseColorARGB(config.fogColorARGB());
            List<float[]> fogStops = List.of(fogColor);
            renderConeLayer(
                    poseStack,
                    apex,
                    direction,
                    (float) config.range(),
                    (float) config.angle(),
                    fogStops,
                    (float) config.fogSizeScale(),
                    (float) config.fogCenterAlpha(),
                    (float) config.fogEdgeAlpha(),
                    fogColor,
                    0.0f,
                    false,
                    irisShaderPackInUse
            );
        }
    }

    private static ConeRenderConfig buildGlobalConeConfig() {
        return new ConeRenderConfig(
                Config.LIGHT_RANGE.get(),
                Config.LIGHT_ANGLE.get(),
                ColorUtils.parseColorStops(Config.LIGHT_COLORS_ARGB.get()),
                Config.LAYER_SIZE_SCALES.get(),
                Config.LAYER_CENTER_ALPHAS.get(),
                Config.LAYER_EDGE_ALPHAS.get(),
                Config.LAYER_COLORS_ARGB.get(),
                Config.COLOR_NOISE_AMPLITUDE.get(),
                Config.CONE_RAYCAST.get(),
                Config.FOG_ENABLED.get(),
                Config.FOG_SIZE_SCALE.get(),
                Config.FOG_CENTER_ALPHA.get(),
                Config.FOG_EDGE_ALPHA.get(),
                Config.FOG_COLOR_ARGB.get()
        );
    }

    private static ConeRenderConfig buildLampConeConfig(LampDeviceConfig cfg) {
        LampDeviceConfig.FogSettings fog = cfg.fog();
        return new ConeRenderConfig(
                cfg.lightRange(),
                cfg.lightAngle(),
                ColorUtils.parseColorStops(cfg.lightColorsARGB()),
                cfg.layerSizeScales(),
                cfg.layerCenterAlphas(),
                cfg.layerEdgeAlphas(),
                cfg.layerColorsARGB(),
                cfg.colorNoiseAmplitude(),
                cfg.coneRaycast(),
                fog.enabled(),
                fog.sizeScale(),
                fog.centerAlpha(),
                fog.edgeAlpha(),
                fog.colorARGB()
        );
    }

    private static void renderConeLayer(
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
            float noiseAmplitude,
            boolean doRaycast,
            boolean irisShaderPackInUse
    ) {
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngleRad = (float) Math.toRadians(baseAngleDeg * sizeScale / 2.0f);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));

        Vec3 upReference = new Vec3(0, 1, 0);
        if (Math.abs(direction.dot(upReference)) > 0.99) {
            upReference = new Vec3(0, 0, 1);
        }
        Vec3 rightVec = upReference.cross(direction).normalize();
        Vec3 orthoUp = direction.cross(rightVec).normalize();

        float[] centerColor = layerColorOverride != null ? layerColorOverride : ColorUtils.colorAt(colorStops, 0.0f);

        long seed = Double.doubleToLongBits(apex.x)
                ^ Double.doubleToLongBits(apex.y)
                ^ Double.doubleToLongBits(apex.z)
                ^ Double.doubleToLongBits(direction.x)
                ^ Double.doubleToLongBits(direction.y)
                ^ Double.doubleToLongBits(direction.z);

        RenderType renderType = irisShaderPackInUse ? IRIS_RAY_CONE_RENDER_TYPE : RAY_CONE_RENDER_TYPE;
        try (ByteBufferBuilder byteBuffer = new ByteBufferBuilder(renderType.bufferSize())) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBuffer);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            PoseStack.Pose pose = poseStack.last();
            vertexConsumer.addVertex(pose, (float) apex.x, (float) apex.y, (float) apex.z).setColor(toArgb(centerColor, centerAlpha));

            Minecraft mc = Minecraft.getInstance();
            for (int i = 0; i <= SEGMENTS; i++) {
                double theta = 2.0 * Math.PI * i / SEGMENTS;
                double cos = Math.cos(theta);
                double sin = Math.sin(theta);
                Vec3 basePoint = baseCenter.add(rightVec.scale(scaledRadius * cos)).add(orthoUp.scale(scaledRadius * sin));

                if (doRaycast && mc.level != null) {
                    HitResult hitResult = mc.level.clip(
                            new ClipContext(apex, basePoint, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, EMPTY_COLLISION)
                    );
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        basePoint = hitResult.getLocation();
                    }
                }

                float thetaNorm = (float) (i / (double) SEGMENTS);
                float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
                float[] edgeColor = ColorUtils.colorAtWithNoise(colorStops, baseT, thetaNorm, seed, noiseAmplitude);
                float alphaLocal = edgeAlpha * (0.85f + 0.15f * ((float) Math.sin(thetaNorm * 11.0 + seed * 0.001) * 0.5f + 0.5f));
                vertexConsumer.addVertex(pose, (float) basePoint.x, (float) basePoint.y, (float) basePoint.z).setColor(toArgb(edgeColor, alphaLocal));
            }

            bufferSource.endBatch(renderType);
        }
    }

    private static int toArgb(float[] rgb, float alpha) {
        int a = clampToByte(alpha * 255.0f);
        int r = clampToByte(rgb[0] * 255.0f);
        int g = clampToByte(rgb[1] * 255.0f);
        int b = clampToByte(rgb[2] * 255.0f);
        return ARGB.color(a, r, g, b);
    }

    private static int clampToByte(float value) {
        return Math.max(0, Math.min(255, Math.round(value)));
    }

    private static float parseFloat(String value, float fallback) {
        try {
            if (value == null) {
                return fallback;
            }
            return Float.parseFloat(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }
        return Math.min(value, 1f);
    }
}
