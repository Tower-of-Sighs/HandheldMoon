package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class FullMoonItem extends BlockItem {
    public FullMoonItem() {
        super(ModBlocks.FULL_MOON.get(), RegistryPropertiesCompat.withId(new Properties(), "item", "full_moon"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        ItemTooltips.addLines(builder, "tooltip.handheldmoon.full_moon.1");
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.remove(ModDataComponent.FULL_MOON_CONFIG.get());
        return stack;
    }
}
