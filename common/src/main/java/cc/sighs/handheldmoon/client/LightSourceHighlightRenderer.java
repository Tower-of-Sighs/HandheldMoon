package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.lights.FullMoonDynamicLightSource;
import cc.sighs.handheldmoon.registry.ModItems;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

/**
 * Draws a see-through box around every light-source entity in the player's
 * field of view while the moonlight wand is held. The outline ignores depth so
 * sources stay visible through terrain, matching the wand's long-range tuning
 * role.
 */
public final class LightSourceHighlightRenderer {
    private static final int OUTLINE_COLOR = 0xCCB8ECFF;
    private static final double MIN_VIEW_DOT = 0.35;

    private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "pipeline/light_source_highlight"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
            .build();

    private static final RenderType RENDER_TYPE = RenderType.create(
            "handheldmoon_light_source_highlight",
            RenderSetup.builder(PIPELINE).createRenderSetup()
    );

    private static final ThreadLocal<ByteBufferBuilder> BUFFERS = ThreadLocal.withInitial(
            () -> new ByteBufferBuilder(RENDER_TYPE.bufferSize())
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
            if (!(entity instanceof FullMoonDynamicLightSource) || entity.isRemoved()) {
                continue;
            }
            Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
            if (to.lengthSqr() < 1.0e-4 || to.normalize().dot(direction) < MIN_VIEW_DOT) {
                continue;
            }
            any = true;
            break;
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
        try {
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBuffer);
            VertexConsumer consumer = bufferSource.getBuffer(RENDER_TYPE);
            PoseStack.Pose pose = poseStack.last();
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof FullMoonDynamicLightSource) || entity.isRemoved()) {
                    continue;
                }
                Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
                if (to.lengthSqr() < 1.0e-4 || to.normalize().dot(direction) < MIN_VIEW_DOT) {
                    continue;
                }
                drawBox(consumer, pose, entity.getBoundingBox().inflate(0.05));
            }
            bufferSource.endBatch(RENDER_TYPE);
        } finally {
            byteBuffer.discard();
        }
        poseStack.popPose();
        mvStack.popMatrix();
    }

    private static boolean isHoldingWand(Minecraft mc) {
        return mc.player.getMainHandItem().is(ModItems.MOONLIGHT_WAND.get())
                || mc.player.getOffhandItem().is(ModItems.MOONLIGHT_WAND.get());
    }

    private static void drawBox(VertexConsumer consumer, PoseStack.Pose pose, AABB box) {
        double minX = box.minX, minY = box.minY, minZ = box.minZ;
        double maxX = box.maxX, maxY = box.maxY, maxZ = box.maxZ;

        // bottom face
        line(consumer, pose, minX, minY, minZ, maxX, minY, minZ);
        line(consumer, pose, maxX, minY, minZ, maxX, minY, maxZ);
        line(consumer, pose, maxX, minY, maxZ, minX, minY, maxZ);
        line(consumer, pose, minX, minY, maxZ, minX, minY, minZ);
        // top face
        line(consumer, pose, minX, maxY, minZ, maxX, maxY, minZ);
        line(consumer, pose, maxX, maxY, minZ, maxX, maxY, maxZ);
        line(consumer, pose, maxX, maxY, maxZ, minX, maxY, maxZ);
        line(consumer, pose, minX, maxY, maxZ, minX, maxY, minZ);
        // vertical edges
        line(consumer, pose, minX, minY, minZ, minX, maxY, minZ);
        line(consumer, pose, maxX, minY, minZ, maxX, maxY, minZ);
        line(consumer, pose, maxX, minY, maxZ, maxX, maxY, maxZ);
        line(consumer, pose, minX, minY, maxZ, minX, maxY, maxZ);
    }

    private static void line(
            VertexConsumer consumer, PoseStack.Pose pose,
            double x1, double y1, double z1, double x2, double y2, double z2
    ) {
        consumer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(OUTLINE_COLOR);
        consumer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(OUTLINE_COLOR);
    }
}
