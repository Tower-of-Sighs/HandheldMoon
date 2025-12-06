package com.sighs.handheldmoon.compat.trinkets;

import com.sighs.handheldmoon.registry.ModItems;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TrinketsCompat {
    private static final String MOD_ID = "trinkets";
    private static boolean INSTALLED = false;

    public static void init() {
        INSTALLED = FabricLoader.getInstance().isModLoaded(MOD_ID);
        if (INSTALLED) FlashlightRender.register();
    }

    public static boolean isUsingTrinketsFlashlight(Player player) {
        if (INSTALLED) {
            return TrinketsCompatInner.isUsingTrinketsFlashlight(player);
        }
        return false;
    }

    public static void toggleTrinketsFlashlight(Player player) {
        if (INSTALLED) {
            TrinketsCompatInner.toggleTrinketsFlashlight(player);
        }
    }

    public static ItemStack getFirstFlashlight(Player player) {
        if (INSTALLED) {
            return TrinketsCompatInner.getFirstFlashlight(player);
        }
        return ModItems.MOONLIGHT_LAMP.getDefaultInstance();
    }
}