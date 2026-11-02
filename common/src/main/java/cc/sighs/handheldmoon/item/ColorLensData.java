package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Loader/version-neutral color-lens data helpers, kept outside the item class
 * so 1.21.1 can supply its own item class with the legacy tooltip signature.
 */
public final class ColorLensData {
    /** The lens color used by freshly crafted lenses. */
    public static final int DEFAULT_COLOR = 0x00FFFF;

    private ColorLensData() {
    }

    /** Returns the stored lens color as 0xRRGGBB. */
    public static int getColor(ItemStack stack) {
        return ItemData.lensColor(stack) & 0xFFFFFF;
    }

    /** Returns a lens stack carrying {@code color}. */
    public static ItemStack create(int color) {
        ItemStack stack = new ItemStack(ModItems.COLOR_LENS.get());
        ItemData.setLensColor(stack, color & 0xFFFFFF);
        return stack;
    }
}
