package cc.sighs.handheldmoon.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightEntityTint;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Tints entity models by the dynamic light color, mirroring block vertex
 * tinting. 1.20.1 builds entity models through {@code ModelPart.Cube#compile},
 * which writes the colour as four floats into {@code VertexConsumer#vertex}.
 */
@Mixin(ModelPart.Cube.class)
public abstract class EntityModelTintMixin {
    @ModifyArgs(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(FFFFFFFFFIIFFF)V"
                    )
    )
    private void handheldmoon$tintCube(Args args) {
        int coefficient = DynamicLightEntityTint.coefficient();
        if (coefficient == -1) {
            return;
        }
        args.set(3, (Float) args.get(3) * ((coefficient >> 16 & 0xFF) / 255.0f));
        args.set(4, (Float) args.get(4) * ((coefficient >> 8 & 0xFF) / 255.0f));
        args.set(5, (Float) args.get(5) * ((coefficient & 0xFF) / 255.0f));
    }
}
