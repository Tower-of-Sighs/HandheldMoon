package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.world.item.Item;
import cc.sighs.handheldmoon.spi.RegistrySupplier;

public final class ModItems {
    public static final RegistrySupplier<? extends Item> MOONLIGHT_LAMP =
            HandheldMoon.registry().registerMoonlightLampItem("moonlight_lamp");
    public static final RegistrySupplier<? extends Item> FULL_MOON =
            HandheldMoon.registry().registerFullMoonItem("full_moon");

    private ModItems() {
    }
}
