package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.spi.PlatformServices;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import java.util.List;

/** 1.20.1 moonlight lamp: no data components, per-stack state comes from {@link ItemData}. */
public class MoonlightLampItem extends BlockItem {
    public MoonlightLampItem() {
        super(ModBlocks.MOONLIGHT_LAMP.get(), new Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        ItemTooltips.addLines(lines,
                "tooltip.handheldmoon.moonlight_lamp.1",
                "tooltip.handheldmoon.moonlight_lamp.2");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                PlatformServices.require().client().requestOpenFlashlightConfig();
            }
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        ItemStack itemStack = player.getItemInHand(hand);
        togglePowered(itemStack);
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            if (context.getLevel().getBlockState(context.getClickedPos()).is(ModBlocks.MOONLIGHT_LAMP.get())) {
                // A placed lamp opens its own configuration instead.
                return InteractionResult.PASS;
            }
            if (context.getLevel().isClientSide()) {
                PlatformServices.require().client().requestOpenFlashlightConfig();
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        ItemData.setPowered(stack, 0);
        ItemData.removeLampConfig(stack);
        return stack;
    }

    public static void togglePowered(ItemStack stack) {
        ItemData.setPowered(stack, ItemData.powered(stack) ^ 1);
    }


    public static int getPowered(ItemStack stack) {
        return ItemData.powered(stack);
    }

    public static boolean isPowered(ItemStack stack) {
        return getPowered(stack) != 0;
    }
}
