package com.sighs.handheldmoon.item;

import com.sighs.handheldmoon.registry.ModBlocks;
import com.sighs.handheldmoon.registry.ModDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoonlightLampItem extends BlockItem {
    public MoonlightLampItem() {
        super(ModBlocks.MOONLIGHT_LAMP, new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        togglePowered(itemStack);
        return super.use(level, player, hand);
    }

    public static void togglePowered(ItemStack stack) {
        int powered = stack.getOrDefault(ModDataComponent.POWERED, 0);
        int newValue = powered ^ 1;
        stack.set(ModDataComponent.POWERED, newValue);
    }


    public static int getPowered(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.POWERED, 0);
    }
}