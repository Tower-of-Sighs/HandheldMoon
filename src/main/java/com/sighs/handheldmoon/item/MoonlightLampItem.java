package com.sighs.handheldmoon.item;

import com.sighs.handheldmoon.registry.ModDataComponent;
import com.sighs.handheldmoon.util.RegisterHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoonlightLampItem extends Item {
    public MoonlightLampItem() {
        super(new Properties().stacksTo(1).setId(RegisterHelper.itemKey("moonlight_lamp")));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
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