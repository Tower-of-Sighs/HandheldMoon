package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** 1.21.1 variant of the color lens, using the legacy tooltip signature. */
public class ColorLensItem extends Item {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        ItemTooltips.addLines(lines,
                "tooltip.handheldmoon.color_lens.1",
                "tooltip.handheldmoon.color_lens.2");
    }

    public static int getColor(ItemStack stack) {
        return ColorLensData.getColor(stack);
    }

    public static ItemStack create(int color) {
        return ColorLensData.create(color);
    }
}
