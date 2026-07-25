package cc.sighs.handheldmoon.compat.accessory;

import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.oelib.platform.Platform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ServiceLoader;

public final class AccessoryCompat {
    private static final String[] MOD_IDS = {"curios", "trinkets"};
    private static final IAccessoryCompat NOOP = new IAccessoryCompat() {
        @Override
        public boolean isUsingAccessoryFlashlight(Player player) {
            return false;
        }

        @Override
        public boolean hasAccessoryFlashlight(Player player) {
            return false;
        }

        @Override
        public void toggleAccessoryFlashlight(Player player) {
        }

        @Override
        public ItemStack getFirstAccessoryFlashlight(Player player) {
            return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
        }
    };

    private static IAccessoryCompat provider = NOOP;
    private static boolean installed = false;

    private AccessoryCompat() {
    }

    public static void init() {
        installed = false;
        for (String modId : MOD_IDS) {
            if (Platform.isModLoaded(modId)) {
                installed = true;
                break;
            }
        }
        if (installed) {
            provider = ServiceLoader.load(IAccessoryCompat.class).findFirst().orElse(NOOP);
            provider.initClient();
        } else {
            provider = NOOP;
        }
    }

    public static boolean isUsingAccessoryFlashlight(Player player) {
        return installed && provider.isUsingAccessoryFlashlight(player);
    }

    public static boolean hasAccessoryFlashlight(Player player) {
        return installed && provider.hasAccessoryFlashlight(player);
    }

    public static void toggleAccessoryFlashlight(Player player) {
        if (installed) {
            provider.toggleAccessoryFlashlight(player);
        }
    }

    public static ItemStack getFirstAccessoryFlashlight(Player player) {
        return installed ? provider.getFirstAccessoryFlashlight(player) : ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }
}
