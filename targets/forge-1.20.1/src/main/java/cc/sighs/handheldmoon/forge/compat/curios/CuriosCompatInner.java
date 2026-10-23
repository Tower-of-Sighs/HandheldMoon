package cc.sighs.handheldmoon.forge.compat.curios;

import cc.sighs.handheldmoon.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

/** Direct Curios 1.20.1 API usage; only loaded when Curios is installed. */
public final class CuriosCompatInner {
    private CuriosCompatInner() {
    }

    public static boolean isUsingCuriosFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                    if (MoonlightLampItem.getPowered(slotResult.stack()) == 1) {
                        result[0] = true;
                    }
                }));
        return result[0];
    }

    public static boolean hasCuriosFlashlight(Player player) {
        boolean[] result = {false};
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findCurios(Utils::isFlashlight).forEach(slotResult -> {
                    if (Utils.isFlashlight(slotResult.stack())) {
                        result[0] = true;
                    }
                }));
        return result[0];
    }

    public static void toggleCuriosFlashlight(Player player) {
        CuriosApi.getCuriosInventory(player).ifPresent(handler ->
                handler.findCurios(Utils::isFlashlight).forEach(slotResult ->
                        MoonlightLampItem.togglePowered(slotResult.stack())));
    }

    public static ItemStack getFirstFlashlight(Player player) {
        ItemStack[] result = {ModItems.MOONLIGHT_LAMP.get().getDefaultInstance()};
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            List<SlotResult> found = handler.findCurios(Utils::isFlashlight);
            if (!found.isEmpty()) {
                result[0] = found.get(0).stack();
            }
        });
        return result[0];
    }
}
