package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.util.Utils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RayEvent {
    private static final Map<UUID, Vec3> LAST_DIR = new HashMap<>();
    private static final float VIEW_ANGLE_DEG = 56.0f;
    private static final float VIEW_RANGE = 14.0f;
    private static final int SEGMENTS = 32; // 段数
    private static final float CONE_R = 1.0f;  // 白色
    private static final float CONE_G = 1.0f;
    private static final float CONE_B = 1.0f;

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(RayEvent::renderPlayerViewConesWithRadialGradient);
    }

    public static void renderPlayerViewConesWithRadialGradient(WorldRenderContext context) {
        if (!Config.PLAYER_RAY.get()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(true);
        PoseStack poseStack = context.matrixStack();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableCull();

        poseStack.pushPose();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        List<AbstractClientPlayer> players = mc.level.players();

        for (Player player : players) {
            if (player.getUUID().equals(Minecraft.getInstance().player.getUUID())) continue;

            if (!Utils.isUsingFlashlight(player)) continue;

            Vec3 eyePos = player.getEyePosition(partialTick);
            Vec3 viewVecRaw = player.getViewVector(partialTick).normalize();
            Vec3 prev = LAST_DIR.getOrDefault(player.getUUID(), viewVecRaw);
            Vec3 viewVec = prev.scale(0.7).add(viewVecRaw.scale(0.3)).normalize();
            LAST_DIR.put(player.getUUID(), viewVec);

            renderCones(poseStack, eyePos, viewVec);
        }

        for (BlockPos pos : HandheldMoonDynamicLightsInitializer.getActiveLampPositions()) {
            var be = mc.level.getBlockEntity(pos);
            if (be instanceof MoonlightLampBlockEntity lamp && lamp.getPowered()) {
                Vec3 eyePos = pos.getCenter();
                Vec3 viewVec = lamp.getViewVec().normalize().scale(-1);
                renderCones(poseStack, eyePos, viewVec);
            }
        }

        poseStack.popPose();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
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

        RenderSystem.setShader(CoreShaders.POSITION_COLOR);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        // 顶点 - 使用最高透明度（中心最亮）
        buffer.addVertex(matrix, (float) apex.x, (float) apex.y, (float) apex.z)
                .setColor(CONE_R, CONE_G, CONE_B, centerAlpha);

        // 添加底面圆周点 - 使用最低透明度（边缘最暗/透明）
        for (int i = 0; i <= SEGMENTS; i++) {
            double theta = 2.0 * Math.PI * i / SEGMENTS;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            Vec3 basePoint = baseCenter
                    .add(rightVec.scale(scaledRadius * cos))
                    .add(orthoUp.scale(scaledRadius * sin));

            if (Config.CONE_RAYCAST.get()) {
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

            buffer.addVertex(matrix, (float) basePoint.x, (float) basePoint.y, (float) basePoint.z)
                    .setColor(CONE_R, CONE_G, CONE_B, edgeAlpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}