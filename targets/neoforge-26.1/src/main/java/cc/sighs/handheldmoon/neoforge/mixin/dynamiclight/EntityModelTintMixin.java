package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightEntityTint;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Tints entity models by the dynamic light color, mirroring block vertex tinting. */
@Mixin(ModelFeatureRenderer.class)
public abstract class EntityModelTintMixin {
    @ModifyVariable(method = "renderModel", at = @At("HEAD"), argsOnly = true)
    private SubmitNodeStorage.ModelSubmit<?> handheldmoon$tintEntityModel(
            SubmitNodeStorage.ModelSubmit<?> submit
    ) {
        return DynamicLightEntityTint.tintEntityModel(submit);
    }
}
