package com.sighs.handheldmoon.compat.curios;

import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.registry.ModItems;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public class CuriosCompatInner {
    public static boolean isUsingCuriosFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                if (MoonlightLampItem.getPowered(slotResult.stack()) == 1) {
                    result[0] = true;
                }
            });
        });
        return result[0];
    }

    public static boolean hasCuriosFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                if (Utils.isFlashlight(slotResult.stack())) result[0] = true;
            });
        });
        return result[0];
    }

    public static void toggleCuriosFlashlight(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                MoonlightLampItem.togglePowered(slotResult.stack());
            });
        });
    }

    public static ItemStack getFirstFlashlight(Player player) {
        ItemStack[] itemStack = {ModItems.MOONLIGHT_LAMP.get().getDefaultInstance()};
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            List<SlotResult> list = iCuriosItemHandler.findCurios(Utils::isFlashlight);
            if (!list.isEmpty()) itemStack[0] = list.getFirst().stack();
        });
        return itemStack[0];
    }
}
