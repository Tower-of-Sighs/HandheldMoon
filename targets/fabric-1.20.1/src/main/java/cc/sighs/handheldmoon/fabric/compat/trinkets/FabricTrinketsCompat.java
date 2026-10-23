package cc.sighs.handheldmoon.fabric.compat.trinkets;

import cc.sighs.handheldmoon.compat.accessory.IAccessoryCompat;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.Utils;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Trinkets 3.7.2 (1.20.1) integration; nothing here runs when Trinkets is absent. */
public final class FabricTrinketsCompat implements IAccessoryCompat {
    @Override
    public boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("trinkets");
    }

    @Override
    public void initClient() {
        if (isAvailable()) {
            FlashlightRender.register();
        }
    }

    @Override
    public boolean isUsingAccessoryFlashlight(Player player) {
        TrinketComponent component = component(player);
        if (component == null) {
            return false;
        }
        for (Tuple<SlotReference, ItemStack> entry : component.getEquipped(Utils::isFlashlight)) {
            if (MoonlightLampItem.getPowered(entry.getB()) == 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAccessoryFlashlight(Player player) {
        TrinketComponent component = component(player);
        return component != null && component.isEquipped(Utils::isFlashlight);
    }

    @Override
    public void toggleAccessoryFlashlight(Player player) {
        TrinketComponent component = component(player);
        if (component == null) {
            return;
        }
        for (Tuple<SlotReference, ItemStack> entry : component.getEquipped(Utils::isFlashlight)) {
            MoonlightLampItem.togglePowered(entry.getB());
        }
    }

    @Override
    public ItemStack getFirstAccessoryFlashlight(Player player) {
        TrinketComponent component = component(player);
        if (component != null) {
            List<Tuple<SlotReference, ItemStack>> equipped = component.getEquipped(Utils::isFlashlight);
            if (!equipped.isEmpty()) {
                return equipped.get(0).getB();
            }
        }
        return ModItems.MOONLIGHT_LAMP.get().getDefaultInstance();
    }

    private static TrinketComponent component(Player player) {
        return TrinketsApi.getTrinketComponent(player).orElse(null);
    }
}
