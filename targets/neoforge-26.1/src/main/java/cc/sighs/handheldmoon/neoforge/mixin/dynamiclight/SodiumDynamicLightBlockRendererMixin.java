package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import net.caffeinemc.mods.sodium.api.util.ColorARGB;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tints Sodium-rendered block vertices by the dynamic light color. Sodium
 * replaces the vanilla block renderer, so the BlockModelLighter mixin never
 * runs when Sodium is installed; this covers the Sodium path instead.
 *
 * <p>The {@code pos} field lives in the parent class
 * {@code AbstractBlockRenderContext} and cannot be {@code @Shadow}ed, so it
 * is read reflectively.
 */
@Mixin(value = BlockRenderer.class, priority = 900)
public abstract class SodiumDynamicLightBlockRendererMixin {
    @Unique
    private java.lang.reflect.Field handheldmoon$posField;

    @Inject(
            method = "processQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void handheldmoon$tintQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        BlockPos lightPos = handheldmoon$pos();
        if (lightPos == null) {
            return;
        }
        int coefficient = DynamicLightRenderHelper.tintCoefficient(lightPos);
        if (coefficient == -1) {
            return;
        }
        // Sodium stores quad vertex colors in ABGR. bufferQuad later calls
        // ColorARGB.toABGR(baseColor) before writing the vertex, so the value
        // we store with setColor must be in ABGR form. Work in ARGB space,
        // then convert back to ABGR before storing.
        for (int i = 0; i < 4; i++) {
            int baseABGR = quad.baseColor(i);
            // ABGR -> ARGB (reverse of ColorARGB.toABGR).
            int baseARGB = ColorARGB.fromABGR(baseABGR);
            int tintedARGB = DynamicLightRenderHelper.multiplyArgb(baseARGB, coefficient);
            // ARGB -> ABGR for storage; bufferQuad converts back once more.
            int tintedABGR = ColorARGB.toABGR(tintedARGB);
            quad.setColor(i, tintedABGR);
        }
    }

    @Unique
    private BlockPos handheldmoon$pos() {
        try {
            if (handheldmoon$posField == null) {
                Class<?> cls = getClass();
                while (cls != null && !cls.getName().contains("AbstractBlockRenderContext")) {
                    cls = cls.getSuperclass();
                }
                if (cls == null) {
                    return null;
                }
                handheldmoon$posField = cls.getDeclaredField("pos");
                handheldmoon$posField.setAccessible(true);
            }
            return (BlockPos) handheldmoon$posField.get(this);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
