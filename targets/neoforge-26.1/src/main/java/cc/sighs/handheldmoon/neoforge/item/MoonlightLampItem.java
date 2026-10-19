package cc.sighs.handheldmoon.neoforge.item;

import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.spi.PlatformServices;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MoonlightLampItem extends BlockItem {
    public MoonlightLampItem() {
        super(ModBlocks.MOONLIGHT_LAMP.get(), RegistryPropertiesCompat.withId(new Properties().stacksTo(1), "item", "moonlight_lamp"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) {
                PlatformServices.require().client().requestOpenFlashlightConfig();
            }
            return InteractionResult.SUCCESS;
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
                // A placed lamp opens its own device configuration instead.
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
        stack.set(ModDataComponent.POWERED.get(), 0);
        stack.remove(ModDataComponent.LAMP_CONFIG.get());
        return stack;
    }

    public static void togglePowered(ItemStack stack) {
        int powered = stack.getOrDefault(ModDataComponent.POWERED.get(), 0);
        int newValue = powered ^ 1;
        stack.set(ModDataComponent.POWERED.get(), newValue);
    }


    public static int getPowered(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.POWERED.get(), 0);
    }

    public static boolean isPowered(ItemStack stack) {
        return getPowered(stack) != 0;
    }
}
