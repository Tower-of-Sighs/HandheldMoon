package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class RayEvent {
    private static final float VIEW_ANGLE_DEG = 56.0f;
    private static final float VIEW_RANGE = 14.0f;
    private static final int SEGMENTS = 32; // 段数
    private static final float CONE_R = 1.0f;  // 白色
    private static final float CONE_G = 1.0f;
    private static final float CONE_B = 1.0f;

    public static void renderPlayerViewConesWithRadialGradient(WorldExtractionContext context, @Nullable HitResult result) {

    }
    // 多层径向渐变圆锥
    public static void renderCones(PoseStack poseStack, Vec3 apex, Vec3 direction) {
        renderConeLayer(poseStack, apex, direction, 1.0f, 0.15f, 0.0f);
        renderConeLayer(poseStack, apex, direction, 1.08f, 0.12f, 0.0f);
        renderConeLayer(poseStack, apex, direction, 1.16f, 0.08f, 0.0f);
    }

    public static void renderConeLayer(PoseStack poseStack, Vec3 apex, Vec3 direction,
                                       float sizeScale, float centerAlpha, float edgeAlpha) {
        // 缩放后的圆锥参数
        float scaledRange = VIEW_RANGE * sizeScale;
        float scaledHalfAngleRad = (float) Math.toRadians(VIEW_ANGLE_DEG * sizeScale / 2.0f);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);

        // 底面中心点
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));

        // 局部坐标系
        Vec3 upReference = new Vec3(0, 1, 0);
        Vec3 rightVec, orthoUp;
        if (Math.abs(direction.dot(upReference)) > 0.99) {
            upReference = new Vec3(0, 0, 1);
        }
        rightVec = upReference.cross(direction).normalize();
        orthoUp = direction.cross(rightVec).normalize();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        // 使用三角形扇绘制径向渐变
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // 顶点 - 使用最高透明度（中心最亮）
        buffer.addVertex(matrix, (float)apex.x, (float)apex.y, (float)apex.z)
                .setColor(CONE_R, CONE_G, CONE_B, centerAlpha);

        // 添加底面圆周点 - 使用最低透明度（边缘最暗/透明）
        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = 2.0 * Math.PI * i / SEGMENTS;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            Vec3 basePoint = baseCenter.add(rightVec.scale(scaledRadius * cos))
                    .add(orthoUp.scale(scaledRadius * sin));

            // 圆周点使用边缘透明度
            buffer.addVertex((float)basePoint.x, (float)basePoint.y, (float)basePoint.z)
                    .setColor(CONE_R, CONE_G, CONE_B, edgeAlpha);
        }

    }
}