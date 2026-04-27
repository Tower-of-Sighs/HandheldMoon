package cc.sighs.handheldmoon.api.raycone.impl;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
            float centerAlpha = clamp01(config.layerCenterAlpha(i));
            float edgeAlpha = clamp01(config.layerEdgeAlpha(i));
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
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngleRad = (float) Math.toRadians(baseAngleDeg * sizeScale / 2.0);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));

        // up/reference vectors for the cone ring
        Vec3 upRef = new Vec3(0, 1, 0);
        if (Math.abs(direction.dot(upRef)) > 0.99) upRef = new Vec3(0, 0, 1);
        Vec3 right = upRef.cross(direction).normalize();
        Vec3 up = direction.cross(right).normalize();

        float[] centerColor = layerColorOverride != null
                ? layerColorOverride
                : colorAt(colorStops, 0.0f);

        long seed = Double.doubleToLongBits(apex.x)
                ^ Double.doubleToLongBits(apex.y)
                ^ Double.doubleToLongBits(apex.z)
                ^ Double.doubleToLongBits(direction.x)
                ^ Double.doubleToLongBits(direction.y)
                ^ Double.doubleToLongBits(direction.z);

        try (ByteBufferBuilder byteBuffer = new ByteBufferBuilder(renderType.bufferSize())) {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBuffer);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            PoseStack.Pose pose = poseStack.last();

            // Apex vertex
            vertexConsumer.addVertex(pose, (float) apex.x, (float) apex.y, (float) apex.z)
                    .setColor(toArgb(centerColor, centerAlpha));

            Minecraft mc = Minecraft.getInstance();
            for (int i = 0; i <= SEGMENTS; i++) {
                double theta = 2.0 * Math.PI * i / SEGMENTS;
                double cos = Math.cos(theta);
                double sin = Math.sin(theta);
                Vec3 bp = baseCenter.add(right.scale(scaledRadius * cos)).add(up.scale(scaledRadius * sin));

                if (doRaycast && mc.level != null) {
                    HitResult hit = mc.level.clip(
                            new ClipContext(apex, bp, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, EMPTY_COLLISION)
                    );
                    if (hit.getType() == HitResult.Type.BLOCK) {
                        bp = hit.getLocation();
                    }
                }

                float thetaNorm = (float) (i / (double) SEGMENTS);
                float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
                float[] edgeColor = colorAtWithNoise(colorStops, baseT, thetaNorm, seed, noiseAmplitude);
                float alphaLocal = edgeAlpha * (0.85f + 0.15f * (float) (
                        Math.sin(thetaNorm * 11.0 + seed * 0.001) * 0.5 + 0.5
                ));

                vertexConsumer.addVertex(pose, (float) bp.x, (float) bp.y, (float) bp.z)
                        .setColor(toArgb(edgeColor, alphaLocal));
            }

            bufferSource.endBatch(renderType);
        }
    }

    // ---- colour helpers ----

    static float[] colorAt(List<float[]> stops, float t) {
        if (stops.isEmpty()) return new float[]{1, 1, 1};
        if (stops.size() == 1) return stops.getFirst();
        float tt = Math.max(0.0f, Math.min(1.0f, t));
        float pos = tt * (stops.size() - 1);
        int i0 = (int) Math.floor(pos);
        int i1 = Math.min(stops.size() - 1, i0 + 1);
        float w = pos - i0;
        float[] a = stops.get(i0);
        float[] b = stops.get(i1);
        return new float[]{
                a[0] + (b[0] - a[0]) * w,
                a[1] + (b[1] - a[1]) * w,
                a[2] + (b[2] - a[2]) * w
        };
    }

    static float[] colorAtWithNoise(List<float[]> stops, float baseT, float thetaNorm, long seed, float amplitude) {
        if (amplitude <= 0) return colorAt(stops, baseT);
        float n1 = (float) Math.sin(thetaNorm * 7.23 + seed * 0.001);
        float n2 = (float) Math.sin(thetaNorm * 13.69 + seed * 0.002);
        float n3 = (float) Math.sin(thetaNorm * 19.41 + seed * 0.0007);
        float n = 0.5f + 0.20f * n1 + 0.20f * n2 + 0.10f * n3;
        float wobble = (n - 0.5f) * 2f * amplitude;
        return colorAt(stops, Math.max(0.0f, Math.min(1.0f, baseT + wobble)));
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

    static float clamp01(float v) {
        return v < 0 ? 0 : Math.min(v, 1f);
    }
}
