package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.compat.curios.CuriosCompat;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Utils {
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (isFlashlight(player.getMainHandItem())) {
            result = isPoweredFlashlight(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            result = isPoweredFlashlight(player.getOffhandItem());
        }
//        return result || CuriosCompat.isUsingCuriosFlashlight(player) || TaczCompat.isUsingAttachmentFlashlight(player);
        return result || CuriosCompat.isUsingCuriosFlashlight(player);
    }

    public static boolean isFlashlight(ItemStack itemStack) {
        return itemStack.is(ModItems.MOONLIGHT_LAMP.get());
    }

    public static boolean isPoweredFlashlight(ItemStack itemStack) {
        return MoonlightLampItem.getPowered(itemStack) == 1;
    }

    public static void toggleFlashlight(Player player) {
        if (isFlashlight(player.getMainHandItem())) {
            MoonlightLampItem.togglePowered(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            MoonlightLampItem.togglePowered(player.getOffhandItem());
        }
        CuriosCompat.toggleCuriosFlashlight(player);
//        TaczCompat.toggleAttachmentFlashlight(player);
    }
}
