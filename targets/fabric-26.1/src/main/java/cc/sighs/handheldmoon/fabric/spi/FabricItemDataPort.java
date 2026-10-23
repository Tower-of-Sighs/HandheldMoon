package cc.sighs.handheldmoon.fabric.spi;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ColorLensData;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.spi.ItemDataPort;
import net.minecraft.world.item.ItemStack;

/** 26.1 item data storage through data components. */
public final class FabricItemDataPort implements ItemDataPort {
    @Override
    public int powered(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.POWERED.get(), 0);
    }

    @Override
    public void setPowered(ItemStack stack, int value) {
        stack.set(ModDataComponent.POWERED.get(), value);
    }

    @Override
    public int lensColor(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.LENS_COLOR.get(), ColorLensData.DEFAULT_COLOR);
    }

    @Override
    public void setLensColor(ItemStack stack, int color) {
        stack.set(ModDataComponent.LENS_COLOR.get(), color);
    }

    @Override
    public boolean hasLampConfig(ItemStack stack) {
        return stack.has(ModDataComponent.LAMP_CONFIG.get());
    }

    @Override
    public LampDeviceConfig lampConfig(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.LAMP_CONFIG.get(), LampDeviceConfig.fromGlobalConfig());
    }

    @Override
    public void setLampConfig(ItemStack stack, LampDeviceConfig config) {
        stack.set(ModDataComponent.LAMP_CONFIG.get(), config);
    }

    @Override
    public void removeLampConfig(ItemStack stack) {
        stack.remove(ModDataComponent.LAMP_CONFIG.get());
    }

    @Override
    public boolean hasFullMoonConfig(ItemStack stack) {
        return stack.has(ModDataComponent.FULL_MOON_CONFIG.get());
    }

    @Override
    public FullMoonDeviceConfig fullMoonConfig(ItemStack stack) {
        return stack.getOrDefault(ModDataComponent.FULL_MOON_CONFIG.get(), FullMoonDeviceConfig.fromGlobalConfig());
    }

    @Override
    public void setFullMoonConfig(ItemStack stack, FullMoonDeviceConfig config) {
        stack.set(ModDataComponent.FULL_MOON_CONFIG.get(), config);
    }

    @Override
    public void removeFullMoonConfig(ItemStack stack) {
        stack.remove(ModDataComponent.FULL_MOON_CONFIG.get());
    }
}
