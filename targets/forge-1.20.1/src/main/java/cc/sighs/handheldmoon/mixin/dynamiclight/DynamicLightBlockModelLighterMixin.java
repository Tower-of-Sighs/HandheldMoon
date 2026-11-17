package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies dynamic-light color to vanilla block quads. 1.20.1 has no
 * {@code BlockModelLighter}; {@code ModelBlockRenderer#putQuadData} is where
 * both the flat and the smooth-lighting paths emit a quad.
 */
@Mixin(value = ModelBlockRenderer.class, priority = 900)
public abstract class DynamicLightBlockModelLighterMixin {
    @Shadow
    @Final
    private BlockColors blockColors;

    @Inject(method = "putQuadData", at = @At("HEAD"), cancellable = true)
    private void handheldmoon$tintQuad(
            BlockAndTintGetter level, BlockState state, BlockPos pos, VertexConsumer consumer,
            PoseStack.Pose pose, BakedQuad quad,
            float shade0, float shade1, float shade2, float shade3,
            int light0, int light1, int light2, int light3, int overlay,
            CallbackInfo callback
    ) {
        if (!DynamicLightManager.hasSourcesNear(pos)) return;
        var face = quad.getDirection();
        BlockPos lightPos = state.isCollisionShapeFullBlock(level, pos) ? pos.relative(face) : pos;
        int[] vertices = quad.getVertices();
        int stride = vertices.length / 4;
        int[] lights = {light0, light1, light2, light3};
        int[] tintedVertices = null;
        for (int i = 0; i < 4; i++) {
            int offset = i * stride;
            int coefficient = DynamicLightRenderHelper.tintCoefficientForVertex(
                    level::getBrightness, lightPos, face,
                    Float.intBitsToFloat(vertices[offset]),
                    Float.intBitsToFloat(vertices[offset + 1]),
                    Float.intBitsToFloat(vertices[offset + 2]), lights[i]);
            if (coefficient == -1) continue;
            if (tintedVertices == null) tintedVertices = vertices.clone();
            // Baked vertex colors are ABGR; the light coefficient is ARGB.
            int abgr = (coefficient & 0xFF00FF00)
                    | (coefficient & 0xFF) << 16 | (coefficient >>> 16 & 0xFF);
            tintedVertices[offset + 3] = DynamicLightRenderHelper.multiplyArgb(vertices[offset + 3], abgr);
        }
        if (tintedVertices == null) return;
        BakedQuad tintedQuad = new BakedQuad(tintedVertices, quad.getTintIndex(), face,
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        if (quad.isTinted()) {
            int tint = this.blockColors.getColor(state, level, pos, quad.getTintIndex());
            red = (tint >> 16 & 0xFF) / 255.0f;
            green = (tint >> 8 & 0xFF) / 255.0f;
            blue = (tint & 0xFF) / 255.0f;
        }

        consumer.putBulkData(
                pose, tintedQuad,
                new float[]{shade0, shade1, shade2, shade3},
                red, green, blue,
                lights,
                overlay, true
        );
        callback.cancel();
    }
}
