package com.sighs.handheldmoon.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import toni.sodiumdynamiclights.DynamicLightSource;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class RayEvent {
    private static final float VIEW_ANGLE_DEG = 56.0f;
    private static final float VIEW_RANGE = 14.0f;
    private static final int SEGMENTS = 32; // 段数
    private static final float CONE_R = 1.0f;  // 白色
    private static final float CONE_G = 1.0f;
    private static final float CONE_B = 1.0f;

    @SubscribeEvent
    public static void renderPlayerViewConesWithRadialGradient(RenderLevelStageEvent event) {
        if (!Config.PLAYER_RAY.get()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float partialTick = mc.getFrameTime();
        PoseStack poseStack = event.getPoseStack();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();

        poseStack.pushPose();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (DynamicLightSource dynamicLightSource : LightEvent.getLightSourceList()) {
            if (dynamicLightSource instanceof Entity entity) {
                if (entity.getUUID().equals(Minecraft.getInstance().player.getUUID())) continue;
                Vec3 eyePos = entity.getEyePosition(partialTick);
                Vec3 viewVec = entity.getViewVector(partialTick).normalize();
                renderCones(poseStack, eyePos, viewVec);
            }
            if (dynamicLightSource instanceof MoonlightLampBlockEntity lamp) {
                Vec3 eyePos = lamp.getBlockPos().getCenter();
                Vec3 viewVec = lamp.getViewVec().normalize();
                renderCones(poseStack, eyePos, viewVec);
            }
        }

        poseStack.popPose();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
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
