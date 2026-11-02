package cc.sighs.handheldmoon.spi;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.world.item.ItemStack;

/**
 * Loader/version-neutral access to the data HandheldMoon stores on item stacks.
 *
 * <p>Minecraft 1.20.5+ stores this in data components, while 1.20.1 still uses
 * item NBT. Implementations are supplied per target through {@link java.util.ServiceLoader}.</p>
 */
public interface ItemDataPort {
    int powered(ItemStack stack);

    void setPowered(ItemStack stack, int value);

    /** Returns the stored lens color as 0xRRGGBB. */
    int lensColor(ItemStack stack);

    void setLensColor(ItemStack stack, int color);

    boolean hasLampConfig(ItemStack stack);

    /** Returns the stored lamp config, or the global default when absent. */
    LampDeviceConfig lampConfig(ItemStack stack);

    void setLampConfig(ItemStack stack, LampDeviceConfig config);

    void removeLampConfig(ItemStack stack);

    boolean hasFullMoonConfig(ItemStack stack);

    /** Returns the stored full-moon config, or the global default when absent. */
    FullMoonDeviceConfig fullMoonConfig(ItemStack stack);

    void setFullMoonConfig(ItemStack stack, FullMoonDeviceConfig config);

    void removeFullMoonConfig(ItemStack stack);
}
