package cc.sighs.handheldmoon.neoforge.mixin.dynamiclight;

import cc.sighs.handheldmoon.neoforge.compat.sodium.SodiumBlockRenderContextAccess;
import net.caffeinemc.mods.sodium.client.render.frapi.render.AbstractBlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractBlockRenderContext.class)
public abstract class SodiumBlockRenderContextAccessMixin implements SodiumBlockRenderContextAccess {
    @Shadow
    protected BlockAndTintGetter level;

    @Shadow
    protected BlockPos pos;

    @Override
    public BlockAndTintGetter handheldmoon$level() {
        return this.level;
    }

    @Override
    public BlockPos handheldmoon$pos() {
        return this.pos;
    }
}
