package cc.sighs.handheldmoon.neoforge.mixin.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.renderer.PostPass;

@Mixin(PostPass.class)
public interface PostPassAccessor {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> handheldmoon$getCustomUniforms();
}
