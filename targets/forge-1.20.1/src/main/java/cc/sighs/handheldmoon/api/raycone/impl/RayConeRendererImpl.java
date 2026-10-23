package cc.sighs.handheldmoon.api.raycone.impl;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeGeometry;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.List;

/**
 * 1.20.1 backend for the ray-cone API. It renders the shared
 * {@link RayConeGeometry} meshes through the legacy fixed-function
 * position-color shader: prepared layers are drawn as triangle lists and
 * raycast-clipped layers fall back to a triangle fan.
 * <p>
 * Installed via {@link RayConeRenderer#installBackend} during client setup.
 * 1.20.1 has no render-pipeline API, so the Iris pipeline hand-off of the 26.1
 * implementation is not available here. The camera transform is part of the
 * level pose stack and the model-view uniform stays identity in 1.20.1, so the
 * world-space offset is applied to the pose stack and {@code modelViewMatrix} is
 * unused.
 */
public final class RayConeRendererImpl {
    private static final int SEGMENTS = 24;

    private RayConeRendererImpl() {
    }

    public static void render(
            PoseStack poseStack, Vec3 cameraPos, Matrix4fc modelViewMatrix,
            List<RayConeRenderer.ConeSource> sources
    ) {
        if (sources.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        try {
            for (RayConeRenderer.ConeSource source : sources) {
                renderSource(poseStack, source);
            }
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    private static void renderSource(PoseStack poseStack, RayConeRenderer.ConeSource source) {
        IRayConeConfig config = source.config();
        for (int i = 0; i < config.layerCount(); i++) {
            renderLayer(
                    poseStack,
                    source.apex(),
                    source.direction(),
                    (float) config.range(),
                    (float) config.angle(),
                    config.colorStops(),
                    config.layerSizeScale(i),
                    RayConeGeometry.clamp01(config.layerCenterAlpha(i)),
                    RayConeGeometry.clamp01(config.layerEdgeAlpha(i)),
                    config.layerColor(i),
                    (float) config.noiseAmplitude(),
                    config.coneRaycast() && (source.raycastAllLayers() || i == 0)
            );
        }

        IRayConeConfig.FogConfig fog = config.fog();
        if (fog.enabled()) {
            renderLayer(
                    poseStack,
                    source.apex(),
                    source.direction(),
                    (float) config.range(),
                    (float) config.angle(),
                    List.of(fog.color()),
                    (float) fog.sizeScale(),
                    (float) fog.centerAlpha(),
                    (float) fog.edgeAlpha(),
                    fog.color(),
                    0.0f,
                    false
            );
        }
    }

    private static void renderLayer(
            PoseStack poseStack,
            Vec3 apex,
            Vec3 direction,
            float baseRange,
            float baseAngleDegrees,
            List<float[]> colorStops,
            float sizeScale,
            float centerAlpha,
            float edgeAlpha,
            float[] layerColorOverride,
            float noiseAmplitude,
            boolean raycast
    ) {
        RayConeGeometry.PreparedLayer prepared = !raycast
                ? RayConeGeometry.preparedLayerNow(baseRange, baseAngleDegrees, sizeScale, SEGMENTS)
                : null;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();
        float[] colorScratch = new float[3];

        if (prepared != null && !prepared.isEmpty()) {
            buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
            RayConeGeometry.emitPreparedLayer(
                    prepared, apex, direction, colorStops,
                    centerAlpha, edgeAlpha, layerColorOverride,
                    noiseAmplitude, colorScratch,
                    (x, y, z, r, g, b, alpha) -> buffer
                            .vertex(matrix, (float) x, (float) y, (float) z)
                            .color(r, g, b, alpha)
                            .endVertex()
            );
        } else {
            buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            RayConeGeometry.emitLayerPrimitive(
                    Minecraft.getInstance(), apex, direction, baseRange, baseAngleDegrees,
                    colorStops, sizeScale, centerAlpha, edgeAlpha, layerColorOverride,
                    noiseAmplitude, raycast, SEGMENTS, colorScratch,
                    (x, y, z, r, g, b, alpha) -> buffer
                            .vertex(matrix, (float) x, (float) y, (float) z)
                            .color(r, g, b, alpha)
                            .endVertex()
            );
        }

        BufferBuilder.RenderedBuffer rendered = buffer.endOrDiscardIfEmpty();
        if (rendered != null) {
            BufferUploader.drawWithShader(rendered);
        }
    }
}
