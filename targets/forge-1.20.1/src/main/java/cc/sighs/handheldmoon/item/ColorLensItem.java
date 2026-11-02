package cc.sighs.handheldmoon.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/** 1.20.1 color lens: the color lives in item NBT through {@link ItemData}. */
public class ColorLensItem extends Item {
    public static final int DEFAULT_COLOR = ColorLensData.DEFAULT_COLOR;

    public ColorLensItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ItemData.setLensColor(stack, DEFAULT_COLOR);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
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
