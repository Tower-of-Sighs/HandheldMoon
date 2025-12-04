package com.sighs.handheldmoon.mixin;

import com.sighs.handheldmoon.api.PostChainAccessor;
import lombok.Getter;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Getter
@Mixin(value = PostChain.class)
@Implements(@Interface(iface = PostChainAccessor.class, prefix = "lazy$"))
public class PostChainMixin implements PostChainAccessor {
    @Shadow
    @Final
    private List<PostPass> passes;

}
