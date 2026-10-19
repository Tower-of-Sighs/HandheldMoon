package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import com.mojang.blaze3d.vertex.QuadInstance;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockModelLighter.class, priority = 900)
public abstract class DynamicLightBlockModelLighterMixin {
    @Inject(method = "prepareQuadFlat", at = @At("TAIL"))
    private void handheldmoon$tintFlatQuad(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            int lightCoords,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        tintSurface(level, state, pos, quad, outputInstance, "FLAT");
    }

    @Inject(method = "prepareQuadAmbientOcclusion", at = @At("TAIL"))
    private void handheldmoon$tintAoQuad(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            BakedQuad quad,
            QuadInstance outputInstance,
            CallbackInfo ci
    ) {
        tintSurface(level, state, pos, quad, outputInstance, "SMOOTH");
    }

    private static void tintSurface(
            BlockAndTintGetter level, BlockState state, BlockPos pos,
            BakedQuad quad, QuadInstance outputInstance, String mode
    ) {
        Direction face = quad.direction();
        BlockPos lightPos = state.isCollisionShapeFullBlock(level, pos)
                ? pos.relative(face) : pos;
        boolean smooth = "SMOOTH".equals(mode);
        int flatTint = smooth ? -1 : DynamicLightRenderHelper.tintCoefficient(level, lightPos);
        for (int i = 0; i < 4; i++) {
            var vertex = quad.position(i);
            int coefficient = smooth ? DynamicLightRenderHelper.tintCoefficientForVertex(
                    level, lightPos, face, vertex.x(), vertex.y(), vertex.z(), outputInstance.getLightCoords(i)
            ) : flatTint;
            outputInstance.setColor(i, DynamicLightRenderHelper.multiplyArgb(
                    outputInstance.getColor(i), coefficient
            ));
        }
    }
}
