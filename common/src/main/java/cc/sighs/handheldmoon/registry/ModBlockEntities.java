package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.world.level.block.entity.BlockEntityType;
import cc.sighs.handheldmoon.spi.RegistrySupplier;

public final class ModBlockEntities {
    public static final RegistrySupplier<? extends BlockEntityType<?>> MOONLIGHT_LAMP =
            HandheldMoon.registry().registerMoonlightLampBlockEntity("moonlight_lamp", ModBlocks.MOONLIGHT_LAMP);
    public static final RegistrySupplier<? extends BlockEntityType<?>> FULL_MOON =
            HandheldMoon.registry().registerFullMoonBlockEntity("full_moon", ModBlocks.FULL_MOON);

    private ModBlockEntities() {
    }
}
