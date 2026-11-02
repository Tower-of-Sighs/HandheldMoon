package cc.sighs.handheldmoon.mixin.client;

import java.util.List;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 1.20.1 keeps {@code PostChain.passes} private; there is no public accessor. */
@Mixin(PostChain.class)
public interface PostChainAccessor {
    @Accessor("passes")
    List<PostPass> handheldmoon$getPasses();
}
