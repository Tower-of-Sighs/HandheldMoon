package com.sighs.handheldmoon.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.function.Predicate;

public final class TrinketUtils {

    public interface StackVisitor {
        void visit(ItemStack stack);
    }

    public static void forEachTrinket(Player player, Predicate<ItemStack> predicate, StackVisitor visitor) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            var results = handler.findCurios(predicate);
            for (SlotResult result : results) {
                ItemStack stack = result.stack();
                if (!stack.isEmpty()) {
                    visitor.visit(stack);
                }
            }
        });
    }
}
