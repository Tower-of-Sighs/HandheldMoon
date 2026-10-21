package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightRenderHelper;
import cc.sighs.handheldmoon.neoforge.compat.EntityTintContext;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Multiplies each model cube color by the entity's dynamic-light coefficient. */
@Mixin(ModelPart.Cube.class)
public abstract class ModelPartCubeTintMixin {
    @ModifyVariable(method = "compile", at = @At("HEAD"), index = 5)
    private int handheldmoon$tintCube(int color) {
        int coefficient = EntityTintContext.coefficient();
        if (coefficient == -1) {
            return color;
        }
        int base = color == -1 ? 0xFFFFFFFF : color;
        return DynamicLightRenderHelper.multiplyArgb(base, coefficient);
    }
}
