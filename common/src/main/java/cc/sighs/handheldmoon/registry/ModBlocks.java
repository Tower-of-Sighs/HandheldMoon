package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlock;
import cc.sighs.handheldmoon.block.MoonlightLampBlock;

public final class ModBlocks {
    public static final cc.sighs.handheldmoon.spi.RegistrySupplier<MoonlightLampBlock> MOONLIGHT_LAMP =
            HandheldMoon.registry().registerBlock("moonlight_lamp", MoonlightLampBlock::new);
    public static final cc.sighs.handheldmoon.spi.RegistrySupplier<FullMoonBlock> FULL_MOON =
            HandheldMoon.registry().registerBlock("full_moon", FullMoonBlock::new);

    private ModBlocks() {
    }
}
