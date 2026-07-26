package cc.sighs.handheldmoon.client.renderer;

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
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

import java.util.List;

public final class LegacyRayConeRenderer {
    private static final int SEGMENTS = 32;

    private LegacyRayConeRenderer() {
    }

    public static void render(PoseStack poseStack, Vec3 cameraPos, Matrix4fc modelViewMatrix, List<RayConeRenderer.ConeSource> sources) {
        if (sources.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        minecraft.getMainRenderTarget().bindWrite(false);
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.set(new Matrix4f(modelViewMatrix)
                .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z));
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();
        poseStack.pushPose();

        for (RayConeRenderer.ConeSource source : sources) {
            renderSource(poseStack, source);
        }

        poseStack.popPose();
        modelViewStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        RayConeGeometry.emitLayer(
                Minecraft.getInstance(), apex, direction, baseRange, baseAngleDegrees,
                colorStops, sizeScale, centerAlpha, edgeAlpha, layerColorOverride,
                noiseAmplitude, raycast, SEGMENTS,
                (position, color, alpha) -> buffer
                        .addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                        .setColor(color[0], color[1], color[2], alpha)
        );

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
