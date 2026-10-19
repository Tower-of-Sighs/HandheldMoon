package cc.sighs.handheldmoon.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import cc.sighs.handheldmoon.spi.RegistrySupplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    public static final RegistrySupplier<MenuType<?>> FLASHLIGHT_CONFIG =
            HandheldMoon.registry().registerMenu("flashlight_config",
                    () -> new MenuType<>(FlashlightConfigMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {
    }
}
