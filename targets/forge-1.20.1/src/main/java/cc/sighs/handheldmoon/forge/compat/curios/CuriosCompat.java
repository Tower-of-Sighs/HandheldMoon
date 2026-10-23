package cc.sighs.handheldmoon.forge.compat.curios;

import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/** Curios compatibility facade; nothing here runs when Curios is absent. */
public final class CuriosCompat {
    private static final String MOD_ID = "curios";
    private static boolean installed;

    private CuriosCompat() {
    }

    public static void init() {
        installed = ModList.get().isLoaded(MOD_ID);
        if (installed) {
            FlashlightRender.register();
        }
    }

    public static boolean isUsingCuriosFlashlight(Player player) {
        return installed && CuriosCompatInner.isUsingCuriosFlashlight(player);
    }

    public static boolean hasCuriosFlashlight(Player player) {
        return installed && CuriosCompatInner.hasCuriosFlashlight(player);
    }

    public static void toggleCuriosFlashlight(Player player) {
        if (installed) {
            CuriosCompatInner.toggleCuriosFlashlight(player);
        }
    }

    public static ItemStack getFirstFlashlight(Player player) {
        if (installed) {
            return CuriosCompatInner.getFirstFlashlight(player);
        }
        return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }
}
