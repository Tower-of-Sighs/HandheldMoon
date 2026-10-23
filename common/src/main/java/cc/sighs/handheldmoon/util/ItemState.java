package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.item.ItemData;
import net.minecraft.world.item.ItemStack;

public final class ItemState {
    private ItemState() {
    }

    public static int powered(ItemStack stack) {
        return ItemData.powered(stack);
    }

    public static void togglePowered(ItemStack stack) {
        ItemData.setPowered(stack, powered(stack) ^ 1);
    }
}
