package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/** 1.20.1 full-moon block item: tooltips use the List-based signature. */
public class FullMoonItem extends BlockItem {
    public FullMoonItem() {
        super(ModBlocks.FULL_MOON.get(), new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        ItemTooltips.addLines(lines, "tooltip.handheldmoon.full_moon.1");
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ItemData.removeFullMoonConfig(stack);
        return stack;
    }
}
