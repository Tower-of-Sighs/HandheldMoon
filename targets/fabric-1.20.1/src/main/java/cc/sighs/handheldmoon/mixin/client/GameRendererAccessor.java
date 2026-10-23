package cc.sighs.handheldmoon.mixin.client;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 1.20.1 has no {@code CrossFrameResourcePool} to hand to a chain, so the
 * closest legacy counterpart is the vanilla post-effect chain handle.
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Accessor("postEffect")
    PostChain handheldmoon$getPostEffect();
}
