package com.sighs.handheldmoon.compat;

import com.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class CuriosCompat {
    private static final String MOD_ID = "curios";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = ModList.get().isLoaded(MOD_ID);
        if (INSTALLED) FlashlightRender.register();
    }

    public static boolean isUsingCuriosFlashlight(Player player) {
        if (INSTALLED) {
            return CuriosCompatInner.isUsingCuriosFlashlight(player);
        }
        return false;
    }

    public static void toggleCuriosFlashlight(Player player) {
        if (INSTALLED) {
            CuriosCompatInner.toggleCuriosFlashlight(player);
        }
    }

    public static ItemStack getFirstFlashlight(Player player) {
        if (INSTALLED) {
            return CuriosCompatInner.getFirstFlashlight(player);
        }
        return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }
}
