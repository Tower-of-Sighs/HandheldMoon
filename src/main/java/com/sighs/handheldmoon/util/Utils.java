package com.sighs.handheldmoon.util;

import com.sighs.handheldmoon.compat.TrinketsCompat;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Utils {
    public static boolean isUsingFlashlight(Player player) {
        boolean result = false;
        if (isFlashlight(player.getMainHandItem())) {
            result = isPoweredFlashlight(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            result = isPoweredFlashlight(player.getOffhandItem());
        }
        return result || TrinketsCompat.isUsingTrinketsFlashlight(player);
    }

    public static boolean isFlashlight(ItemStack itemStack) {
        return itemStack.is(ModItems.MOONLIGHT_LAMP);
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
        TrinketsCompat.toggleTrinketsFlashlight(player);
    }
}