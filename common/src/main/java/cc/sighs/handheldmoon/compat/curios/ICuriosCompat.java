package cc.sighs.handheldmoon.compat.curios;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ICuriosCompat {
    boolean isUsingCuriosFlashlight(Player player);

    boolean hasCuriosFlashlight(Player player);

    void toggleCuriosFlashlight(Player player);

    ItemStack getFirstFlashlight(Player player);
}
