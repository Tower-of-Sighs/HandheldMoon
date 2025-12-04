package com.sighs.handheldmoon.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class TaczCompat {
    private static final String MOD_ID = "tacz";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = ModList.get().isLoaded(MOD_ID);
        if (INSTALLED) FlashlightRender.register();
    }

    public static boolean isUsingAttachmentFlashlight(Player player) {
        if (INSTALLED) {
            return TaczCompatInner.isUsingAttachmentFlashlight(player);
        }
        return false;
    }

    public static void toggleAttachmentFlashlight(Player player) {
        if (INSTALLED) {
            TaczCompatInner.toggleAttachmentFlashlight(player);
        }
    }

    public static boolean isLampAttachment(ItemStack itemStack) {
        if (INSTALLED) {
            return TaczCompatInner.isLampAttachment(itemStack);
        }
        return false;
    }
}
