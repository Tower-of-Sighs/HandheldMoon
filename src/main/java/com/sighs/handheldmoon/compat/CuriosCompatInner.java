package com.sighs.handheldmoon.compat;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Items;
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
    public static void toggleCuriosFlashlight(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            iCuriosItemHandler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                MoonlightLampItem.togglePowered(slotResult.stack());
            });
        });
    }
    public static ItemStack getFirstFlashlight(Player player) {
        ItemStack[] itemStack = {Items.MOONLIGHT_LAMP.get().getDefaultInstance()};
        CuriosApi.getCuriosInventory(player).ifPresent(iCuriosItemHandler -> {
            List<SlotResult> list = iCuriosItemHandler.findCurios(Utils::isFlashlight);
            if (!list.isEmpty()) itemStack[0] = list.get(0).stack();
        });
        return itemStack[0];
    }
}
