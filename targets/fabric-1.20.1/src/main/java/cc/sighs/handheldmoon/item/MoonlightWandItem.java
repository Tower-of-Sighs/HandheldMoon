package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.client.LightSourceWand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/**
 * 1.20.1 moonlight wand: opens the light-source target editor on the client
 * and returns the legacy {@link InteractionResultHolder} result.
 */
public class MoonlightWandItem extends Item {
    public MoonlightWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        ItemTooltips.addLines(lines,
                "tooltip.handheldmoon.moonlight_wand.1",
                "tooltip.handheldmoon.moonlight_wand.2");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            LightSourceWand.openTarget(player);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
