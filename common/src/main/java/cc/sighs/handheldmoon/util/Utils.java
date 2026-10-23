package cc.sighs.handheldmoon.util;

import cc.sighs.handheldmoon.compat.FlashlightCompatHooks;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ItemData;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class Utils {
    private Utils() {
    }

    public static boolean isUsingFlashlight(Player player) {
        boolean held = isFlashlight(player.getMainHandItem())
                && isPoweredFlashlight(player.getMainHandItem());
        held |= isFlashlight(player.getOffhandItem())
                && isPoweredFlashlight(player.getOffhandItem());
        return held || FlashlightCompatHooks.isUsingFlashlight(player);
    }

    public static boolean isFlashlight(ItemStack itemStack) {
        return itemStack.is(ModItems.MOONLIGHT_LAMP.get());
    }

    public static boolean isPoweredFlashlight(ItemStack itemStack) {
        return ItemState.powered(itemStack) == 1;
    }

    public static void toggleFlashlight(Player player) {
        if (isFlashlight(player.getMainHandItem())) {
            ItemState.togglePowered(player.getMainHandItem());
        }
        if (isFlashlight(player.getOffhandItem())) {
            ItemState.togglePowered(player.getOffhandItem());
        }
        FlashlightCompatHooks.toggleFlashlight(player);
    }

    /**
     * Returns the device config carried by the player's held moonlight lamp,
     * preferring the main hand, or {@code null} when no lamp is held.
     * <p>
     * A lamp without its own {@code LAMP_CONFIG} component falls back to the
     * global device config, mirroring the device config screen.
     */
    public static @Nullable LampDeviceConfig heldLampConfig(Player player) {
        LampDeviceConfig main = stackLampConfig(player.getMainHandItem());
        return main != null ? main : stackLampConfig(player.getOffhandItem());
    }

    /** Returns the held lamp config, or the global default when no lamp is held. */
    public static LampDeviceConfig heldLampConfigOrDefault(Player player) {
        LampDeviceConfig config = heldLampConfig(player);
        return config != null ? config : LampDeviceConfig.fromGlobalConfig();
    }

    private static @Nullable LampDeviceConfig stackLampConfig(ItemStack stack) {
        if (!stack.is(ModItems.MOONLIGHT_LAMP.get())) {
            return null;
        }
        return ItemData.lampConfig(stack);
    }
}
