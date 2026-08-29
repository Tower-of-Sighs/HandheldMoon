package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.registry.ModDataComponent;
import net.minecraft.world.item.ItemStack;

public final class ItemState {
    private ItemState() {
    }

    public static int powered(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.POWERED.get(), 0);
    }

    public static void togglePowered(ItemStack stack) {
        stack.set(ModDataComponent.POWERED.get(), powered(stack) ^ 1);
    }
}
