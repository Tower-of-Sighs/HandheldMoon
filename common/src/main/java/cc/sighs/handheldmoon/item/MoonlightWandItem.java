package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.client.LightSourceWand;
import cc.sighs.handheldmoon.util.RegistryPropertiesCompat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * Utility wand: while held it highlights light-source entities in view, and
 * right-clicking one opens its light profile editor regardless of distance.
 */
public class MoonlightWandItem extends Item {
    public MoonlightWandItem() {
        super(RegistryPropertiesCompat.withId(new Properties().stacksTo(1), "item", "moonlight_wand"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            LightSourceWand.openTarget(player);
        }
        return InteractionResult.SUCCESS;
    }
}
