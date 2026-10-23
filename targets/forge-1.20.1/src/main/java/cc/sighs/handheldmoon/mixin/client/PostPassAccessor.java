package cc.sighs.handheldmoon.mixin.client;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 1.20.1 exposes {@code PostPass#getEffect()} already; the accessor is kept for
 * parity with the newer targets, where the shader instance is field-only.
 */
@Mixin(PostPass.class)
public interface PostPassAccessor {
    @Accessor("effect")
    EffectInstance handheldmoon$getEffect();
}
