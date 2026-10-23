package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
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
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;
        if (quad.isTinted()) {
            int tint = this.blockColors.getColor(state, level, pos, quad.getTintIndex());
            red = (tint >> 16 & 0xFF) / 255.0f;
            green = (tint >> 8 & 0xFF) / 255.0f;
            blue = (tint & 0xFF) / 255.0f;
        }

        int coefficient = DynamicLightRenderHelper.tintCoefficient(level::getBrightness, pos);
        if (coefficient != -1) {
            red *= (coefficient >> 16 & 0xFF) / 255.0f;
            green *= (coefficient >> 8 & 0xFF) / 255.0f;
            blue *= (coefficient & 0xFF) / 255.0f;
        }

        consumer.putBulkData(
                pose, quad,
                new float[]{shade0, shade1, shade2, shade3},
                red, green, blue,
                new int[]{light0, light1, light2, light3},
                overlay, true
        );
        callback.cancel();
    }
}
