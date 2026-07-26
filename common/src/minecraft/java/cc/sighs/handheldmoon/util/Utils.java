package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class Utils {
    private Utils() {
    }

    public static boolean isUsingFlashlight(Player player) {
        boolean held = isFlashlight(player.getMainHandItem())
                && isPoweredFlashlight(player.getMainHandItem());
        held |= isFlashlight(player.getOffhandItem())
                && isPoweredFlashlight(player.getOffhandItem());
        return held || FlashlightCompatHooks.isUsingFlashlight(player);
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
        FlashlightCompatHooks.toggleFlashlight(player);
    }
}
