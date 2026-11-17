package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import cc.sighs.handheldmoon.dynamiclight.SodiumVertexLighting;
import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.caffeinemc.mods.sodium.client.model.light.data.QuadLightData;
import net.caffeinemc.mods.sodium.client.model.light.flat.FlatLightPipeline;
import net.caffeinemc.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import net.caffeinemc.mods.sodium.client.model.quad.ModelQuadView;
import net.caffeinemc.mods.sodium.client.render.frapi.mesh.MutableQuadViewImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Preserve Sodium's AO and sky interpolation, then sample our flashlight at the surface. */
@Mixin(value = {SmoothLightPipeline.class, FlatLightPipeline.class}, remap = false)
public abstract class SodiumLightPipelineMixin {
    @Shadow(remap = false) @Final private LightDataAccess lightCache;

    @Inject(method = "calculate", at = @At("RETURN"), remap = false)
    private void handheldmoon$lightVertices(
            ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace,
            Direction face, boolean shade, boolean enhanced, CallbackInfo ci
    ) {
        if (quad instanceof MutableQuadViewImpl mutable && mutable.material().emissive()) return;
        var level = lightCache.getLevel();
        BlockPos lightPos = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? pos.relative(face) : pos;
        boolean smooth = (Object) this instanceof SmoothLightPipeline;
        for (int i = 0; i < 4; i++) {
            int baseline = quad instanceof MutableQuadViewImpl mutable
                    ? SodiumVertexLighting.maxLight(out.lm[i], mutable.lightmap(i)) : out.lm[i];
            var vertex = SodiumVertexLighting.shade(level::getBrightness, pos, lightPos, face,
                    quad.getX(i), quad.getY(i), quad.getZ(i), baseline, smooth);
            out.lm[i] = vertex.lightmap();
            if (quad instanceof MutableQuadViewImpl mutable) {
                mutable.color(i, DynamicLightRenderHelper.multiplyArgb(mutable.color(i), vertex.tint()));
            }
        }
    }
}
