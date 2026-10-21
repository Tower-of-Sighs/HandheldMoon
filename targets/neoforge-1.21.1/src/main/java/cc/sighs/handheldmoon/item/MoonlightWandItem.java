package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.client.LightSourceWand;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 1.21.1 variant of the moonlight wand. The item signature uses the legacy
 * {@link InteractionResultHolder} return type.
 */
public class MoonlightWandItem extends Item {
    public MoonlightWandItem() {
        super(RegistryPropertiesCompat.withId(new Properties().stacksTo(1), "item", "moonlight_wand"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            LightSourceWand.openTarget(player);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
