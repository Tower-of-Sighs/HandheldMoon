package cc.sighs.handheldmoon.api.raycone.impl;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeGeometry;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.util.IrisCompat;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * Internal rendering implementation for ray cones.
 * <p>
 * Handles vertex geometry, colour computation, raycast clipping,
 * Iris pipeline setup, and model-view stack management.
 */
public final class RayConeRendererImpl {
    private static final int SEGMENTS = 24;

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

    private RayConeRendererImpl() {
    }

    /**
     * Render all cone sources.
     */
    public static void render(
            PoseStack poseStack,
            Vec3 cameraPos,
            Matrix4fc modelViewMatrix,
            List<RayConeRenderer.ConeSource> sources
    ) {
        if (sources.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        boolean irisActive = IrisCompat.isShaderPackInUse();
        if (irisActive) {
            IrisCompat.assignRayConePipeline(IRIS_RAY_CONE_PIPELINE);
        }

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.set(new Matrix4f(modelViewMatrix)).translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        poseStack.pushPose();

        RenderType renderType = irisActive ? IRIS_RAY_CONE_RENDER_TYPE : RAY_CONE_RENDER_TYPE;

        for (RayConeRenderer.ConeSource source : sources) {
            renderSingleCone(poseStack, source, renderType);
        }

        poseStack.popPose();
        mvStack.popMatrix();
    }

    private static void renderSingleCone(
            PoseStack poseStack,
            RayConeRenderer.ConeSource source,
            RenderType renderType
    ) {
        IRayConeConfig config = source.config();
        Vec3 apex = source.apex();
        Vec3 direction = source.direction();
        float baseRange = (float) config.range();
        float baseAngleDeg = (float) config.angle();

        // ---- colour stops ----
        List<float[]> stops = config.colorStops();
        double noiseAmp = config.noiseAmplitude();

        // ---- layers ----
        for (int i = 0; i < config.layerCount(); i++) {
            float sizeScale = config.layerSizeScale(i);
            float centerAlpha = RayConeGeometry.clamp01(config.layerCenterAlpha(i));
            float edgeAlpha = RayConeGeometry.clamp01(config.layerEdgeAlpha(i));
            boolean doRaycast = config.coneRaycast() && (source.raycastAllLayers() || i == 0);

            renderConeLayer(poseStack, apex, direction,
                    baseRange, baseAngleDeg, stops,
                    sizeScale, centerAlpha, edgeAlpha,
                    config.layerColor(i),
                    (float) noiseAmp, doRaycast, renderType);
        }

        // ---- fog layer ----
        IRayConeConfig.FogConfig fog = config.fog();
        if (fog.enabled()) {
            List<float[]> fogStops = List.of(fog.color());
            renderConeLayer(poseStack, apex, direction,
                    baseRange, baseAngleDeg, fogStops,
                    (float) fog.sizeScale(),
                    (float) fog.centerAlpha(),
                    (float) fog.edgeAlpha(),
                    fog.color(),
                    0f, false, renderType);
        }
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
            RenderType renderType
    ) {
        try (ByteBufferBuilder byteBuffer = new ByteBufferBuilder(renderType.bufferSize())) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBuffer);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            PoseStack.Pose pose = poseStack.last();
            RayConeGeometry.emitLayer(
                    Minecraft.getInstance(), apex, direction, baseRange, baseAngleDeg,
                    colorStops, sizeScale, centerAlpha, edgeAlpha, layerColorOverride,
                    noiseAmplitude, doRaycast, SEGMENTS,
                    (position, color, alpha) -> vertexConsumer
                            .addVertex(pose, (float) position.x, (float) position.y, (float) position.z)
                            .setColor(toArgb(color, alpha))
            );

            bufferSource.endBatch(renderType);
        }
    }

    static int toArgb(float[] rgb, float alpha) {
        return ARGB.color(
                clampToByte(alpha * 255f),
                clampToByte(rgb[0] * 255f),
                clampToByte(rgb[1] * 255f),
                clampToByte(rgb[2] * 255f)
        );
    }

    private static int clampToByte(float v) {
        return Math.max(0, Math.min(255, Math.round(v)));
    }
}
