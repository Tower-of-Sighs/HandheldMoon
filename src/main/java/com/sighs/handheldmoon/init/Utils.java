package com.sighs.handheldmoon.init;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (isFlashlight(player.getMainHandItem())) {
            result = MoonlightLampItem.getPowered(player.getMainHandItem()) == 1;
        }
        if (isFlashlight(player.getOffhandItem())) {
            result = MoonlightLampItem.getPowered(player.getOffhandItem()) == 1;
        }
        return result || CuriosCompat.isUsingCuriosFlashlight(player);
    }
    public static boolean isFlashlight(ItemStack itemStack) {
        return itemStack.is(ModItems.MOONLIGHT_LAMP.get());
    }
    public static void toggleFlashlight(Player player) {
        if (isFlashlight(player.getMainHandItem())) {
            MoonlightLampItem.togglePowered(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            MoonlightLampItem.togglePowered(player.getOffhandItem());
        }
        CuriosCompat.toggleCuriosFlashlight(player);

    }
}
