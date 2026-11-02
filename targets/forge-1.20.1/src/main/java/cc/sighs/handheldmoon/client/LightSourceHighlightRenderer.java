package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Draws a see-through box around every light-source entity in the player's
 * field of view while the moonlight wand is held. The outline ignores depth so
 * sources stay visible through terrain, matching the wand's long-range tuning
 * role.
 * <p>
 * 1.20.1 variant: the fixed-function {@link RenderType#lines()} pass replaces
 * the 26.1 line pipeline. In 1.20.1 the camera transform is already part of the
 * level pose stack and the model-view uniform stays identity, so the world-space
 * offset is applied to the pose stack and {@code modelViewMatrix} is unused.
 */
public final class LightSourceHighlightRenderer {
    private static final double MIN_VIEW_DOT = 0.35;
    private static final int OUTLINE_RED = 184;
    private static final int OUTLINE_GREEN = 236;
    private static final int OUTLINE_BLUE = 255;
    private static final int OUTLINE_ALPHA = 204;

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

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.disableDepthTest();
        try {
            BufferBuilder builder = Tesselator.getInstance().getBuilder();
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(builder);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.lines());
            Matrix4f matrix = poseStack.last().pose();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (isVisibleSource(entity, eye, direction)) {
                    drawBox(consumer, matrix, entity.getBoundingBox().inflate(0.05));
                }
            }
            bufferSource.endBatch(RenderType.lines());
        } finally {
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
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

    private static void drawBox(VertexConsumer consumer, Matrix4f matrix, AABB box) {
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        line(consumer, matrix, minX, minY, minZ, maxX, minY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, minY, maxZ);
        line(consumer, matrix, maxX, minY, maxZ, minX, minY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, minY, minZ);
        line(consumer, matrix, minX, maxY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(consumer, matrix, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(consumer, matrix, minX, maxY, maxZ, minX, maxY, minZ);
        line(consumer, matrix, minX, minY, minZ, minX, maxY, minZ);
        line(consumer, matrix, maxX, minY, minZ, maxX, maxY, minZ);
        line(consumer, matrix, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(consumer, matrix, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(
            VertexConsumer consumer, Matrix4f matrix,
            double x1, double y1, double z1, double x2, double y2, double z2
    ) {
        vertex(consumer, matrix, x1, y1, z1);
        vertex(consumer, matrix, x2, y2, z2);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
                .color(OUTLINE_RED, OUTLINE_GREEN, OUTLINE_BLUE, OUTLINE_ALPHA)
                .normal(0.0f, 1.0f, 0.0f)
                .endVertex();
    }
}
