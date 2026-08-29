package cc.sighs.handheldmoon.fabric.item;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MoonlightLampItem extends BlockItem {
    public MoonlightLampItem() {
        super(ModBlocks.MOONLIGHT_LAMP.get(), RegistryPropertiesCompat.withId(new Properties().stacksTo(1), "item", "moonlight_lamp"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        togglePowered(itemStack);
        return super.use(level, player, hand);
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


