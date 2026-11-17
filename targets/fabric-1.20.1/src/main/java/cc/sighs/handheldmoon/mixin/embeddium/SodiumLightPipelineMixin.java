package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.SodiumVertexLighting;
import cc.sighs.handheldmoon.dynamiclight.SodiumVertexTintAccess;
import me.jellysquid.mods.sodium.client.model.light.data.LightDataAccess;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.light.flat.FlatLightPipeline;
import me.jellysquid.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import me.jellysquid.mods.sodium.client.model.quad.ModelQuadView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sodium 0.5 has a six-argument pipeline and immutable baked quads. */
@Mixin(value = {SmoothLightPipeline.class, FlatLightPipeline.class}, remap = false)
public abstract class SodiumLightPipelineMixin {
    @Shadow(remap = false) @Final private LightDataAccess lightCache;

    @Inject(method = "calculate", at = @At("RETURN"), remap = false)
    private void handheldmoon$lightVertices(
            ModelQuadView quad, BlockPos pos, QuadLightData out, Direction cullFace,
            Direction face, boolean shade, CallbackInfo ci
    ) {
        var level = lightCache.getWorld();
        BlockPos lightPos = level.getBlockState(pos).isCollisionShapeFullBlock(level, pos) ? pos.relative(face) : pos;
        boolean smooth = (Object) this instanceof SmoothLightPipeline;
        var tints = (SodiumVertexTintAccess) (Object) out;
        for (int i = 0; i < 4; i++) {
            // Sodium 0.5's Fabric ModelQuadView has no baked-light accessor.
            int baseline = out.lm[i];
            var vertex = SodiumVertexLighting.shade(level::getBrightness, pos, lightPos, face,
                    quad.getX(i), quad.getY(i), quad.getZ(i), baseline, smooth);
            out.lm[i] = vertex.lightmap();
            tints.handheldmoon$setTint(i, vertex.tint());
        }
    }
}
