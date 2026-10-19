package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** A color filter that can be installed into a moonlight lamp flashlight. */
public class ColorLensItem extends Item {
    /** The lens color used by freshly crafted lenses. */
    public static final int DEFAULT_COLOR = 0x00FFFF;

    public ColorLensItem() {
        super(RegistryPropertiesCompat.withId(new Properties().stacksTo(1)
                .component(ModDataComponent.LENS_COLOR_TYPE, DEFAULT_COLOR), "item", "color_lens"));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.set(ModDataComponent.LENS_COLOR.get(), DEFAULT_COLOR);
        return stack;
    }

    /** Returns the stored lens color as 0xRRGGBB. */
    public static int getColor(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.LENS_COLOR.get(), DEFAULT_COLOR) & 0xFFFFFF;
    }

    /** Returns a lens stack carrying {@code color}. */
    public static ItemStack create(int color) {
        ItemStack stack = new ItemStack(cc.sighs.handheldmoon.registry.ModItems.COLOR_LENS.get());
        stack.set(ModDataComponent.LENS_COLOR.get(), color & 0xFFFFFF);
        return stack;
    }
}
