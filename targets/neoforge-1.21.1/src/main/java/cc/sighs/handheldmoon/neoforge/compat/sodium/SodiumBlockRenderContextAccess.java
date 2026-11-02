package cc.sighs.handheldmoon.neoforge.compat.sodium;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/** Exposes Sodium's current block render context to the tint mixin. */
public interface SodiumBlockRenderContextAccess {
    BlockAndTintGetter handheldmoon$level();

    BlockPos handheldmoon$pos();
}
