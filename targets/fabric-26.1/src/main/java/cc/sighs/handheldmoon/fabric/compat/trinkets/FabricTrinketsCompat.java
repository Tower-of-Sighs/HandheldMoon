package cc.sighs.handheldmoon.fabric.compat.trinkets;

import cc.sighs.handheldmoon.compat.accessory.IAccessoryCompat;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.Utils;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FabricTrinketsCompat implements IAccessoryCompat {
    @Override
    public void initClient() {
        FlashlightRender.register();
    }

    @Override
    public boolean isUsingAccessoryFlashlight(Player player) {
        List<Tuple<TrinketSlotAccess, ItemStack>> equipped = TrinketsApi.getAttachment(player)
                .getEquipped(Utils::isFlashlight);
        for (Tuple<TrinketSlotAccess, ItemStack> entry : equipped) {
            if (MoonlightLampItem.getPowered(entry.getB()) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAccessoryFlashlight(Player player) {
        return TrinketsApi.getAttachment(player).isEquipped(Utils::isFlashlight);
    }

    @Override
    public void toggleAccessoryFlashlight(Player player) {
        TrinketsApi.getAttachment(player).getEquipped(Utils::isFlashlight)
                .forEach(entry -> MoonlightLampItem.togglePowered(entry.getB()));
    }

    @Override
    public ItemStack getFirstAccessoryFlashlight(Player player) {
        List<Tuple<TrinketSlotAccess, ItemStack>> equipped = TrinketsApi.getAttachment(player)
                .getEquipped(Utils::isFlashlight);
        if (!equipped.isEmpty()) {
            return equipped.getFirst().getB();
        }
        return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }
}
