package cc.sighs.handheldmoon.item;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.spi.ItemDataPort;
import net.minecraft.world.item.ItemStack;

import java.util.ServiceLoader;

/**
 * Shared entry point for the per-stack data HandheldMoon stores.
 *
 * <p>The storage itself lives in each target's {@link ItemDataPort}: 1.20.5+
 * targets use data components, 1.20.1 uses item NBT.</p>
 */
public final class ItemData {
    private static ItemDataPort port;

    private ItemData() {
    }

    private static ItemDataPort port() {
        if (port == null) {
            port = ServiceLoader.load(ItemDataPort.class).findFirst()
                    .orElseThrow(() -> new IllegalStateException("No HandheldMoon ItemDataPort implementation found"));
        }
        return port;
    }

    public static int powered(ItemStack stack) {
        return port().powered(stack);
    }

    public static void setPowered(ItemStack stack, int value) {
        port().setPowered(stack, value);
    }

    /** Returns the stored lens color as 0xRRGGBB. */
    public static int lensColor(ItemStack stack) {
        return port().lensColor(stack);
    }

    public static void setLensColor(ItemStack stack, int color) {
        port().setLensColor(stack, color);
    }

    public static boolean hasLampConfig(ItemStack stack) {
        return port().hasLampConfig(stack);
    }

    public static LampDeviceConfig lampConfig(ItemStack stack) {
        return port().lampConfig(stack);
    }

    public static void setLampConfig(ItemStack stack, LampDeviceConfig config) {
        port().setLampConfig(stack, config);
    }

    public static void removeLampConfig(ItemStack stack) {
        port().removeLampConfig(stack);
    }

    public static boolean hasFullMoonConfig(ItemStack stack) {
        return port().hasFullMoonConfig(stack);
    }

    public static FullMoonDeviceConfig fullMoonConfig(ItemStack stack) {
        return port().fullMoonConfig(stack);
    }

    public static void setFullMoonConfig(ItemStack stack, FullMoonDeviceConfig config) {
        port().setFullMoonConfig(stack, config);
    }

    public static void removeFullMoonConfig(ItemStack stack) {
        port().removeFullMoonConfig(stack);
    }
}
