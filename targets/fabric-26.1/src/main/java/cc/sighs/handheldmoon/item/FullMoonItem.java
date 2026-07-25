package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class FullMoonItem extends BlockItem {
    public FullMoonItem() {
        super(ModBlocks.FULL_MOON.get(), new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "full_moon"))));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.remove(ModDataComponent.FULL_MOON_CONFIG.get());
        return stack;
    }
}
