package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/** A color filter that can be installed into a moonlight lamp flashlight. */
public class ColorLensItem extends Item {
    /** The lens color used by freshly crafted lenses. */
    public static final int DEFAULT_COLOR = ColorLensData.DEFAULT_COLOR;

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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        ItemTooltips.addLines(builder,
                "tooltip.handheldmoon.color_lens.1",
                "tooltip.handheldmoon.color_lens.2");
    }

    /** Returns the stored lens color as 0xRRGGBB. */
    public static int getColor(ItemStack stack) {
        return ColorLensData.getColor(stack);
    }

    /** Returns a lens stack carrying {@code color}. */
    public static ItemStack create(int color) {
        return ColorLensData.create(color);
    }
}
