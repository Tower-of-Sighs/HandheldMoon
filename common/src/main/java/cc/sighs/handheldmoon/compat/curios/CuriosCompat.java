package cc.sighs.handheldmoon.compat.curios;

import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.oelib.platform.Platform;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ServiceLoader;

public final class CuriosCompat {
    private static final String MOD_ID = "curios";
    private static final ICuriosCompat NOOP = new ICuriosCompat() {
        @Override
        public boolean isUsingCuriosFlashlight(Player player) {
            return false;
        }

        @Override
        public boolean hasCuriosFlashlight(Player player) {
            return false;
        }

        @Override
        public void toggleCuriosFlashlight(Player player) {
        }

        @Override
        public ItemStack getFirstFlashlight(Player player) {
            return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
        }
    };

    private static ICuriosCompat provider = NOOP;
    private static boolean installed = false;

    private CuriosCompat() {
    }

    public static void init() {
        installed = Platform.isModLoaded(MOD_ID);
        if (installed) {
            provider = ServiceLoader.load(ICuriosCompat.class).findFirst().orElse(NOOP);
        } else {
            provider = NOOP;
        }
    }

    public static boolean isUsingCuriosFlashlight(Player player) {
        return installed && provider.isUsingCuriosFlashlight(player);
    }

    public static boolean hasCuriosFlashlight(Player player) {
        return installed && provider.hasCuriosFlashlight(player);
    }

    public static void toggleCuriosFlashlight(Player player) {
        if (installed) {
            provider.toggleCuriosFlashlight(player);
        }
    }

    public static ItemStack getFirstFlashlight(Player player) {
        return installed ? provider.getFirstFlashlight(player) : ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }
}
