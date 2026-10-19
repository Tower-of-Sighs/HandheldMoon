package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.caffeinemc.mods.sodium.client.render.model.MutableQuadViewImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
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
    @Unique
    private java.lang.reflect.Field handheldmoon$levelField;
    @Unique
    private java.lang.reflect.Field handheldmoon$stateField;

    @Inject(
            method = "processQuad",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;bufferQuad(Lnet/caffeinemc/mods/sodium/client/render/model/MutableQuadViewImpl;[FLnet/caffeinemc/mods/sodium/client/render/chunk/terrain/material/Material;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void handheldmoon$tintQuad(MutableQuadViewImpl quad, CallbackInfo ci) {
        BlockPos blockPos = handheldmoon$field("pos");
        BlockAndTintGetter level = handheldmoon$field("level");
        BlockState state = handheldmoon$field("state");
        if (blockPos == null || level == null || state == null) {
            return;
        }
        BlockPos lightPos = state.isCollisionShapeFullBlock(level, blockPos)
                ? blockPos.relative(quad.getLightFace()) : blockPos;
        net.caffeinemc.mods.sodium.client.model.light.LightMode defaultMode = handheldmoon$field("defaultLightMode");
        Boolean ao = handheldmoon$field("useAmbientOcclusion");
        var mode = quad.ambientOcclusion() == net.minecraft.util.TriState.DEFAULT ? defaultMode
                : Boolean.TRUE.equals(ao) && quad.ambientOcclusion() != net.minecraft.util.TriState.FALSE
                ? net.caffeinemc.mods.sodium.client.model.light.LightMode.SMOOTH
                : net.caffeinemc.mods.sodium.client.model.light.LightMode.FLAT;
        boolean smooth = mode == net.caffeinemc.mods.sodium.client.model.light.LightMode.SMOOTH;
        int flatTint = smooth ? -1 : DynamicLightRenderHelper.tintCoefficient(level, lightPos);
        // Sodium keeps MutableQuadView colors in ARGB and converts them to ABGR
        // only inside bufferQuad. Store ARGB here to avoid swapping R/B twice.
        for (int i = 0; i < 4; i++) {
            int coefficient = smooth ? DynamicLightRenderHelper.tintCoefficientForVertex(
                    level, lightPos, quad.getLightFace(), quad.getX(i), quad.getY(i), quad.getZ(i), quad.getLight(i)
            ) : flatTint;
            int baseARGB = quad.baseColor(i);
            int tintedARGB = DynamicLightRenderHelper.multiplyArgb(baseARGB, coefficient);
            quad.setColor(i, tintedARGB);
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    private <T> T handheldmoon$field(String name) {
        try {
            java.lang.reflect.Field field = switch (name) {
                case "pos" -> handheldmoon$posField;
                case "level" -> handheldmoon$levelField;
                case "state" -> handheldmoon$stateField;
                default -> null;
            };
            if (field == null) {
                Class<?> cls = getClass();
                while (cls != null && !cls.getName().contains("AbstractBlockRenderContext")) {
                    cls = cls.getSuperclass();
                }
                if (cls == null) {
                    return null;
                }
                field = cls.getDeclaredField(name);
                field.setAccessible(true);
                switch (name) {
                    case "pos" -> handheldmoon$posField = field;
                    case "level" -> handheldmoon$levelField = field;
                    case "state" -> handheldmoon$stateField = field;
                }
            }
            return (T) field.get(this);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
