package cc.sighs.handheldmoon.neoforge.compat.curios;

import cc.sighs.handheldmoon.compat.accessory.IAccessoryCompat;
import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public class NeoForgeCuriosCompat implements IAccessoryCompat {
    @Override
    public void initClient() {
        FlashlightRender.register();
    }

    @Override
    public boolean isUsingAccessoryFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                if (MoonlightLampItem.getPowered(slotResult.stack()) == 1) {
                    result[0] = true;
                }
            });
        });
        return result[0];
    }

    @Override
    public boolean hasAccessoryFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            handler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                if (Utils.isFlashlight(slotResult.stack())) {
                    result[0] = true;
                }
            });
        });
        return result[0];
    }

    @Override
    public void toggleAccessoryFlashlight(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findCurios(Utils::isFlashlight).forEach(slotResult ->
                        MoonlightLampItem.togglePowered(slotResult.stack())
                )
        );
    }

    @Override
    public ItemStack getFirstAccessoryFlashlight(Player player) {
        ItemStack[] itemStack = {ModItems.MOONLIGHT_LAMP.get().getDefaultInstance()};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            List<SlotResult> list = handler.findCurios(Utils::isFlashlight);
            if (!list.isEmpty()) {
                itemStack[0] = list.getFirst().stack();
            }
        });
        return itemStack[0];
    }
}
