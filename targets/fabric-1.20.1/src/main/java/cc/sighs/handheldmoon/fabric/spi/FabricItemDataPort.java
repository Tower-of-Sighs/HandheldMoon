package cc.sighs.handheldmoon.fabric.spi;

import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ColorLensData;
import cc.sighs.handheldmoon.spi.ItemDataPort;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * 1.20.1 storage for HandheldMoon's per-stack data. Data components only exist
 * from 1.20.5, so this version keeps the same values in item NBT.
 */
public final class FabricItemDataPort implements ItemDataPort {
    private static final String POWERED = "handheldmoon:powered";
    private static final String LENS_COLOR = "handheldmoon:lens_color";
    private static final String LAMP_CONFIG = "handheldmoon:lamp_config";
    private static final String FULL_MOON_CONFIG = "handheldmoon:full_moon_config";

    @Override
    public int powered(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(POWERED);
    }

    @Override
    public void setPowered(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(POWERED, value);
    }

    @Override
    public int lensColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(LENS_COLOR)
                ? tag.getInt(LENS_COLOR) : ColorLensData.DEFAULT_COLOR;
    }

    @Override
    public void setLensColor(ItemStack stack, int color) {
        stack.getOrCreateTag().putInt(LENS_COLOR, color);
    }

    @Override
    public boolean hasLampConfig(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(LAMP_CONFIG);
    }

    @Override
    public LampDeviceConfig lampConfig(ItemStack stack) {
        Tag tag = read(stack, LAMP_CONFIG);
        return tag == null ? LampDeviceConfig.fromGlobalConfig()
                : DeviceConfigCodecs.LAMP.parse(NbtOps.INSTANCE, tag)
                .result().orElseGet(LampDeviceConfig::fromGlobalConfig);
    }

    @Override
    public void setLampConfig(ItemStack stack, LampDeviceConfig config) {
        write(stack, LAMP_CONFIG, DeviceConfigCodecs.LAMP.encodeStart(NbtOps.INSTANCE, config).result().orElse(null));
    }

    @Override
    public void removeLampConfig(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(LAMP_CONFIG);
        }
    }

    @Override
    public boolean hasFullMoonConfig(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(FULL_MOON_CONFIG);
    }

    @Override
    public FullMoonDeviceConfig fullMoonConfig(ItemStack stack) {
        Tag tag = read(stack, FULL_MOON_CONFIG);
        return tag == null ? FullMoonDeviceConfig.fromGlobalConfig()
                : DeviceConfigCodecs.FULL_MOON.parse(NbtOps.INSTANCE, tag)
                .result().orElseGet(FullMoonDeviceConfig::fromGlobalConfig);
    }

    @Override
    public void setFullMoonConfig(ItemStack stack, FullMoonDeviceConfig config) {
        write(stack, FULL_MOON_CONFIG,
                DeviceConfigCodecs.FULL_MOON.encodeStart(NbtOps.INSTANCE, config).result().orElse(null));
    }

    @Override
    public void removeFullMoonConfig(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(FULL_MOON_CONFIG);
        }
    }

    private static Tag read(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        return tag == null ? null : tag.get(key);
    }

    private static void write(ItemStack stack, String key, Tag value) {
        if (value == null) {
            return;
        }
        stack.getOrCreateTag().put(key, value);
    }
}
