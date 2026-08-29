package cc.sighs.handheldmoon.compat.accessory;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ServiceLoader;
import java.util.ServiceConfigurationError;

/** Common facade for optional accessory integrations. */
public final class AccessoryCompat {
    private static final IAccessoryCompat NOOP = new IAccessoryCompat() {
        @Override public boolean isUsingAccessoryFlashlight(Player player) { return false; }
        @Override public boolean hasAccessoryFlashlight(Player player) { return false; }
        @Override public void toggleAccessoryFlashlight(Player player) { }
        @Override public ItemStack getFirstAccessoryFlashlight(Player player) { return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance(); }
    };

    private static IAccessoryCompat provider = NOOP;

    private AccessoryCompat() {
    }

    public static void init() {
        try {
            provider = ServiceLoader.load(IAccessoryCompat.class).findFirst()
                    .filter(IAccessoryCompat::isAvailable).orElse(NOOP);
        } catch (ServiceConfigurationError error) {
            HandheldMoon.LOGGER.debug("Optional accessory integration is unavailable", error);
            provider = NOOP;
        }
        provider.initClient();
    }

    public static boolean isUsingAccessoryFlashlight(Player player) { return provider.isUsingAccessoryFlashlight(player); }
    public static boolean hasAccessoryFlashlight(Player player) { return provider.hasAccessoryFlashlight(player); }
    public static void toggleAccessoryFlashlight(Player player) { provider.toggleAccessoryFlashlight(player); }
    public static ItemStack getFirstAccessoryFlashlight(Player player) { return provider.getFirstAccessoryFlashlight(player); }
}
