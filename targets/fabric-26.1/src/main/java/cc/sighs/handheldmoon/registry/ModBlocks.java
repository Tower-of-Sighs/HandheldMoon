package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.block.MoonlightLampBlock;
import cc.sighs.oelib.registry.DeferredRegister;
import cc.sighs.oelib.registry.RegisterSupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, HandheldMoon.MOD_ID);

    public static final RegisterSupplier<MoonlightLampBlock> MOONLIGHT_LAMP =
            BLOCKS.register("moonlight_lamp", MoonlightLampBlock::new);

    public static final RegisterSupplier<FullMoonBlock> FULL_MOON =
            BLOCKS.register("full_moon", FullMoonBlock::new);
}
