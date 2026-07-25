package cc.sighs.handheldmoon.client.renderer;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.util.ColorUtils;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
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
                    clamp01(config.layerCenterAlpha(i)),
                    clamp01(config.layerEdgeAlpha(i)),
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
        float scaledRange = baseRange * sizeScale;
        float scaledHalfAngle = (float) Math.toRadians(baseAngleDegrees * sizeScale / 2.0f);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngle);
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));
        Vec3 upReference = Math.abs(direction.dot(new Vec3(0, 1, 0))) > 0.99
                ? new Vec3(0, 0, 1)
                : new Vec3(0, 1, 0);
        Vec3 right = upReference.cross(direction).normalize();
        Vec3 up = direction.cross(right).normalize();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = poseStack.last().pose();
        float[] centerColor = layerColorOverride != null ? layerColorOverride : ColorUtils.colorAt(colorStops, 0.0f);
        buffer.addVertex(matrix, (float) apex.x, (float) apex.y, (float) apex.z)
                .setColor(centerColor[0], centerColor[1], centerColor[2], centerAlpha);

        long seed = Double.doubleToLongBits(apex.x)
                ^ Double.doubleToLongBits(apex.y)
                ^ Double.doubleToLongBits(apex.z)
                ^ Double.doubleToLongBits(direction.x)
                ^ Double.doubleToLongBits(direction.y)
                ^ Double.doubleToLongBits(direction.z);

        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = 2.0 * Math.PI * i / SEGMENTS;
            Vec3 basePoint = baseCenter
                    .add(right.scale(scaledRadius * Math.cos(theta)))
                    .add(up.scale(scaledRadius * Math.sin(theta)));
            if (raycast && Minecraft.getInstance().level != null) {
                HitResult hit = Minecraft.getInstance().level.clip(new ClipContext(
                        apex,
                        basePoint,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                ));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    basePoint = hit.getLocation();
                }
            }

            float thetaNormal = (float) (i / (double) SEGMENTS);
            float baseT = Math.min(1.0f, 0.8f + (sizeScale - 1.0f) * 0.6f);
            float[] edgeColor = ColorUtils.colorAtWithNoise(colorStops, baseT, thetaNormal, seed, noiseAmplitude);
            float localAlpha = edgeAlpha * (0.85f + 0.15f * ((float) Math.sin(thetaNormal * 11.0 + seed * 0.001) * 0.5f + 0.5f));
            buffer.addVertex(matrix, (float) basePoint.x, (float) basePoint.y, (float) basePoint.z)
                    .setColor(edgeColor[0], edgeColor[1], edgeColor[2], localAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static float clamp01(float value) {
        return value < 0.0f ? 0.0f : Math.min(value, 1.0f);
    }
}
