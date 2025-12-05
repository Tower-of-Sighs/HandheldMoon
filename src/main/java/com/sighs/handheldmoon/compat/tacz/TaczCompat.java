package com.sighs.handheldmoon.compat.tacz;

import com.tacz.guns.api.item.IGun;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TaczCompat {
    private static final String MOD_ID = "tacz";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = FabricLoader.getInstance().isModLoaded(MOD_ID);
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

    public static boolean hasMoonlightAttachment(ItemStack itemStack) {
        if (INSTALLED) {
            var iGun = IGun.getIGunOrNull(itemStack);
            if (iGun == null) return false;
            return TaczCompatInner.hasMoonlightAttachment(itemStack, iGun);
        }
        return false;
    }
}
