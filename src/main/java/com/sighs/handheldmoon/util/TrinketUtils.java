package com.sighs.handheldmoon.util;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class TrinketUtils {

    public interface StackVisitor {
        void visit(ItemStack stack);
    }

    public static void forEachTrinket(Player player, Predicate<ItemStack> predicate, StackVisitor visitor) {
        TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
            component.getInventory().forEach((groupId, group) -> {
                group.forEach((slotId, slot) -> {
                    for (int i = 0; i < slot.getContainerSize(); i++) {
                        ItemStack stack = slot.getItem(i);
                        if (!stack.isEmpty() && predicate.test(stack)) {
                            visitor.visit(stack);
                        }
                    }
                });
            });
        });
    }
}
