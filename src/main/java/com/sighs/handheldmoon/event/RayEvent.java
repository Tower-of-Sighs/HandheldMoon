package com.sighs.handheldmoon.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.List;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class RayEvent {
    // 常量定义
    private static final float VIEW_ANGLE_DEG = 50.0f;
    private static final float VIEW_RANGE = 16.0f;
    private static final int SEGMENTS = 32;
    private static final float CONE_R = 1.0f;  // 白色
    private static final float CONE_G = 1.0f;
    private static final float CONE_B = 1.0f;

    @SubscribeEvent
    public static void renderPlayerViewConesWithRadialGradient(RenderLevelStageEvent event) {
        if (!Config.PLAYER_RAY.get()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float partialTick = mc.getFrameTime();
        PoseStack poseStack = event.getPoseStack();

        // 设置叠加混合模式，增强光照效果
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();

        // 应用相机变换
        poseStack.pushPose();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // 获取所有玩家
        List<AbstractClientPlayer> players = mc.level.players();

        for (Player player : players) {
            if (player.getUUID().equals(Minecraft.getInstance().player.getUUID())) continue;
            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 viewVec = player.getViewVector(partialTick).normalize();

            // 绘制多层径向渐变圆锥
            renderMultiLayerRadialGradientCone(poseStack, eyePos, viewVec);
        }

        poseStack.popPose();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    /**
     * 绘制多层径向渐变圆锥 - 实现光照衰减效果
     */
    public static void renderMultiLayerRadialGradientCone(PoseStack poseStack, Vec3 apex, Vec3 direction) {
        // 绘制3层径向渐变圆锥，实现模糊+衰减效果
        renderRadialGradientConeLayer(poseStack, apex, direction, 1.0f, 0.15f, 0.0f);  // 内层：顶点25%透明度，边缘0%
        renderRadialGradientConeLayer(poseStack, apex, direction, 1.08f, 0.12f, 0.0f); // 中层：顶点15%透明度，边缘0%
        renderRadialGradientConeLayer(poseStack, apex, direction, 1.16f, 0.08f, 0.0f); // 外层：顶点8%透明度，边缘0%
    }

    /**
     * 绘制单层径向渐变圆锥
     * @param sizeScale 尺寸缩放
     * @param centerAlpha 顶点透明度（最高）
     * @param edgeAlpha 边缘透明度（最低）
     */
    public static void renderRadialGradientConeLayer(PoseStack poseStack, Vec3 apex, Vec3 direction,
                                                     float sizeScale, float centerAlpha, float edgeAlpha) {
        // 计算缩放后的圆锥参数
        float scaledRange = VIEW_RANGE * sizeScale;
        float scaledHalfAngleRad = (float) Math.toRadians(VIEW_ANGLE_DEG * sizeScale / 2.0f);
        float scaledRadius = scaledRange * (float) Math.tan(scaledHalfAngleRad);

        // 计算底面中心点
        Vec3 baseCenter = apex.add(direction.scale(scaledRange));

        // 计算局部坐标系
        Vec3 upReference = new Vec3(0, 1, 0);
        Vec3 rightVec, orthoUp;
        if (Math.abs(direction.dot(upReference)) > 0.99) {
            upReference = new Vec3(0, 0, 1);
        }
        rightVec = upReference.cross(direction).normalize();
        orthoUp = direction.cross(rightVec).normalize();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 使用三角形扇绘制径向渐变
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // 顶点 - 使用最高透明度（中心最亮）
        buffer.vertex(matrix, (float)apex.x, (float)apex.y, (float)apex.z)
                .color(CONE_R, CONE_G, CONE_B, centerAlpha).endVertex();

        // 添加底面圆周点 - 使用最低透明度（边缘最暗/透明）
        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = 2.0 * Math.PI * i / SEGMENTS;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            Vec3 basePoint = baseCenter.add(rightVec.scale(scaledRadius * cos))
                    .add(orthoUp.scale(scaledRadius * sin));

            // 圆周点使用边缘透明度
            buffer.vertex(matrix, (float)basePoint.x, (float)basePoint.y, (float)basePoint.z)
                    .color(CONE_R, CONE_G, CONE_B, edgeAlpha).endVertex();
        }

        tesselator.end();
    }
}
