package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

/**
 * 1.21.1 variant of the wand highlight, using the legacy fixed-function line
 * render type instead of the 26.1 render-pipeline API.
 */
public final class LightSourceHighlightRenderer {
    private static final double MIN_VIEW_DOT = 0.35;
    private static final ThreadLocal<ByteBufferBuilder> BUFFERS = ThreadLocal.withInitial(
            () -> new ByteBufferBuilder(RenderType.lines().bufferSize())
    );

    private LightSourceHighlightRenderer() {
    }

    public static void render(
            PoseStack poseStack, Vec3 cameraPos, Matrix4fc modelViewMatrix, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !isHoldingWand(mc)) {
            return;
        }

        Vec3 eye = mc.player.getEyePosition(partialTick);
        Vec3 direction = mc.player.getViewVector(partialTick).normalize();

        boolean any = false;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (isVisibleSource(entity, eye, direction)) {
                any = true;
                break;
            }
        }
        if (!any) {
            return;
        }

        Matrix4fStack mvStack = RenderSystem.getModelViewStack();
        mvStack.pushMatrix();
        mvStack.set(new Matrix4f(modelViewMatrix))
                .translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);

        poseStack.pushPose();
        ByteBufferBuilder byteBuffer = BUFFERS.get();
        RenderSystem.disableDepthTest();
        try {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBuffer);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
            PoseStack.Pose pose = poseStack.last();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (isVisibleSource(entity, eye, direction)) {
                    drawBox(consumer, pose, entity.getBoundingBox().inflate(0.05));
                }
            }
            bufferSource.endBatch(RenderType.lines());
        } finally {
            RenderSystem.enableDepthTest();
            byteBuffer.discard();
        }
        poseStack.popPose();
        mvStack.popMatrix();
    }

    private static boolean isVisibleSource(Entity entity, Vec3 eye, Vec3 direction) {
        if (!(entity instanceof FullMoonDynamicLightSource) || entity.isRemoved()) {
            return false;
        }
        Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
        return to.lengthSqr() >= 1.0e-4 && to.normalize().dot(direction) >= MIN_VIEW_DOT;
    }

    private static boolean isHoldingWand(Minecraft mc) {
        return mc.player.getMainHandItem().is(ModItems.MOONLIGHT_WAND.get())
                || mc.player.getOffhandItem().is(ModItems.MOONLIGHT_WAND.get());
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, AABB box) {
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        line(consumer, pose, minX, minY, minZ, maxX, minY, minZ);
        line(consumer, pose, maxX, minY, minZ, maxX, minY, maxZ);
        line(consumer, pose, maxX, minY, maxZ, minX, minY, maxZ);
        line(consumer, pose, minX, minY, maxZ, minX, minY, minZ);
        line(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ);
        line(consumer, pose, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(consumer, pose, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(consumer, pose, minX, maxY, maxZ, minX, maxY, minZ);
        line(consumer, pose, minX, minY, minZ, minX, maxY, minZ);
        line(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ);
        line(consumer, pose, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(consumer, pose, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(
            VertexConsumer consumer, PoseStack.Pose pose,
            double x1, double y1, double z1, double x2, double y2, double z2
    ) {
        vertex(consumer, pose, x1, y1, z1);
        vertex(consumer, pose, x2, y2, z2);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, double x, double y, double z) {
        consumer.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(184, 236, 255, 204)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
