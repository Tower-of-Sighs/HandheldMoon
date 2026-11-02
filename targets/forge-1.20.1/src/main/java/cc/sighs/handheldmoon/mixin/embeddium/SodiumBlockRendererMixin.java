package cc.sighs.handheldmoon.mixin.embeddium;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies dynamic-light color to Embeddium / Sodium 0.5 chunk quads.
 *
 * <p>Embeddium and Sodium 0.5 bypass {@code ModelBlockRenderer#putQuadData}, so
 * {@code DynamicLightBlockModelLighterMixin} never sees their geometry. Their
 * light values still come from {@code LevelRenderer#getLightColor} (patched by
 * {@code DynamicLightBrightnessGetterMixin}), which is why the real light shows
 * up bright but uncolored without this hook. Every quad is emitted through
 * {@code BlockRenderer#writeGeometry}, whose per-vertex color array can be
 * multiplied by the light color before it is written.</p>
 */
@Mixin(value = BlockRenderer.class, remap = false)
public abstract class SodiumBlockRendererMixin {
    @Inject(method = "writeGeometry", at = @At("HEAD"), remap = false)
    private void handheldmoon$tintQuad(
            BlockRenderContext context, ChunkModelBuilder buffers, Vec3 origin,
            Material material, BakedQuadView quad, int[] colors, QuadLightData lightData,
            CallbackInfo callback
    ) {
        BlockPos pos = context.pos();
        BlockAndTintGetter level = context.world();
        Direction face = quad.getLightFace();
        int count = Math.min(4, colors.length);
        for (int i = 0; i < count; i++) {
            int coefficient = DynamicLightRenderHelper.tintCoefficientForVertex(
                    level::getBrightness, pos, face,
                    quad.getX(i), quad.getY(i), quad.getZ(i), lightData.lm[i]);
            if (coefficient != -1) {
                colors[i] = DynamicLightRenderHelper.multiplyArgb(colors[i], coefficient);
            }
        }
    }
}
