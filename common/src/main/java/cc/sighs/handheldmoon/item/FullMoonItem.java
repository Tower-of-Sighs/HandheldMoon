package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.registry.ModBlocks;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class FullMoonItem extends BlockItem {
    public FullMoonItem() {
        super(ModBlocks.FULL_MOON.get(), RegistryPropertiesCompat.withId(new Properties(), "item", "full_moon"));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.remove(ModDataComponent.FULL_MOON_CONFIG.get());
        return stack;
    }
}
