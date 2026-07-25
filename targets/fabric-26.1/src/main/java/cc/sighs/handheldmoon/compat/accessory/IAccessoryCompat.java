package cc.sighs.handheldmoon.compat.accessory;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IAccessoryCompat {
    default void initClient() {
    }

    boolean isUsingAccessoryFlashlight(Player player);

    boolean hasAccessoryFlashlight(Player player);

    void toggleAccessoryFlashlight(Player player);

    ItemStack getFirstAccessoryFlashlight(Player player);
}
