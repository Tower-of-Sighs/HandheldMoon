package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import cc.sighs.handheldmoon.dynamiclight.SodiumVertexTintAccess;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Writes the per-vertex tint produced by the Sodium 0.5 light pipeline. */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class SodiumBlockRendererMixin {
    @Inject(method = "writeGeometry", at = @At("HEAD"), remap = false)
    private void handheldmoon$tintQuad(
            BlockRenderContext context, ChunkModelBuilder buffers, Vec3 origin,
            Material material, BakedQuadView quad, int[] colors, QuadLightData lightData,
            CallbackInfo callback
    ) {
        var tints = (SodiumVertexTintAccess) (Object) lightData;
        int count = Math.min(4, colors.length);
        for (int i = 0; i < count; i++) {
            int coefficient = tints.handheldmoon$getTint(i);
            if (coefficient != -1) {
                colors[i] = DynamicLightRenderHelper.multiplyArgb(colors[i], coefficient);
            }
        }
    }
}
