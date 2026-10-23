package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightEntityTint;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Tints entity models by the dynamic light color, mirroring block vertex
 * tinting. 1.20.1 builds entity models through {@code ModelPart.Cube#compile},
 * which writes the colour as four floats into {@code VertexConsumer#vertex}.
 *
 * <p>Implemented with three {@link ModifyArg} injections instead of
 * {@code @ModifyArgs}, so no MixinExtras synthetic-args support is needed.</p>
 */
@Mixin(ModelPart.Cube.class)
public abstract class EntityModelTintMixin {
    private static final String VERTEX =
            "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(FFFFFFFFFIIFFF)V";

    @ModifyArg(method = "compile", at = @At(value = "INVOKE", target = VERTEX), index = 3)
    private float handheldmoon$tintRed(float red) {
        int coefficient = DynamicLightEntityTint.coefficient();
        return coefficient == -1 ? red : red * ((coefficient >> 16 & 0xFF) / 255.0f);
    }

    @ModifyArg(method = "compile", at = @At(value = "INVOKE", target = VERTEX), index = 4)
    private float handheldmoon$tintGreen(float green) {
        int coefficient = DynamicLightEntityTint.coefficient();
        return coefficient == -1 ? green : green * ((coefficient >> 8 & 0xFF) / 255.0f);
    }

    @ModifyArg(method = "compile", at = @At(value = "INVOKE", target = VERTEX), index = 5)
    private float handheldmoon$tintBlue(float blue) {
        int coefficient = DynamicLightEntityTint.coefficient();
        return coefficient == -1 ? blue : blue * ((coefficient & 0xFF) / 255.0f);
    }
}
