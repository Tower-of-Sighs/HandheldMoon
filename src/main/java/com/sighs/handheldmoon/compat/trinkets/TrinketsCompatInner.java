package com.sighs.handheldmoon.compat.trinkets;

import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.registry.ModItems;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompatInner {

    public static boolean isUsingTrinketsFlashlight(Player player) {
        final boolean[] result = {false};

        TrinketUtils.forEachTrinket(player, Utils::isFlashlight, stack -> {
            if (MoonlightLampItem.getPowered(stack) == 1) {
                result[0] = true;
            }
        });

        return result[0];
    }

    public static void toggleTrinketsFlashlight(Player player) {
        TrinketUtils.forEachTrinket(player, Utils::isFlashlight, MoonlightLampItem::togglePowered);
    }

    public static ItemStack getFirstFlashlight(Player player) {
        final ItemStack[] result = {
                ModItems.MOONLIGHT_LAMP.getDefaultInstance()
        };
        final boolean[] found = {false};

        TrinketUtils.forEachTrinket(player, Utils::isFlashlight, stack -> {
            if (!found[0]) {
                result[0] = stack;
                found[0] = true;
            }
        });

        return result[0];
    }
}
