package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.world.item.CreativeModeTab;

public final class ModCreativeModeTab {
    public static final cc.sighs.handheldmoon.spi.RegistrySupplier<CreativeModeTab> CATBURGER_TAB =
            HandheldMoon.registry().registerCreativeModeTab("catburger_tab", ModItems.MOONLIGHT_LAMP, ModItems.FULL_MOON);

    private ModCreativeModeTab() {
    }
}
