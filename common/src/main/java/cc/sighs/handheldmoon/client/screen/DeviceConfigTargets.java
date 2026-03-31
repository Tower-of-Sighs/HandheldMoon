package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class DeviceConfigTargets {
    public interface LampTarget {
        LampDeviceConfig get();

        void apply(LampDeviceConfig config);
    }

    public interface FullMoonTarget {
        FullMoonDeviceConfig get();

        void apply(FullMoonDeviceConfig config);
    }

    private DeviceConfigTargets() {
    }

    public static @Nullable LampTarget cursorLampBlock(Minecraft mc) {
        var lamp = ClientUtils.getCursorMoonlightLampBlock();
        if (lamp == null) {
            return null;
        }
        BlockPos pos = lamp.getBlockPos();
        return new LampTarget() {
            @Override
            public LampDeviceConfig get() {
                if (mc.level == null) {
                    return LampDeviceConfig.fromGlobalConfig();
                }
                if (mc.level.getBlockEntity(pos) instanceof MoonlightLampBlockEntity target) {
                    return target.getLampConfig();
                }
                return LampDeviceConfig.fromGlobalConfig();
            }

            @Override
            public void apply(LampDeviceConfig config) {
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof MoonlightLampBlockEntity target) {
                    target.setLampConfig(config, true);
                    ClientUtils.syncMoonlightLampConfig(pos, config);
                }
            }
        };
    }

    public static @Nullable FullMoonTarget cursorFullMoonBlock(Minecraft mc) {
        var moon = ClientUtils.getCursorFullMoonBlock();
        if (moon == null) {
            return null;
        }
        BlockPos pos = moon.getBlockPos();
        return new FullMoonTarget() {
            @Override
            public FullMoonDeviceConfig get() {
                if (mc.level == null) {
                    return FullMoonDeviceConfig.fromGlobalConfig();
                }
                if (mc.level.getBlockEntity(pos) instanceof FullMoonBlockEntity target) {
                    return target.getFullMoonConfig();
                }
                return FullMoonDeviceConfig.fromGlobalConfig();
            }

            @Override
            public void apply(FullMoonDeviceConfig config) {
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof FullMoonBlockEntity target) {
                    target.setFullMoonConfig(config, true);
                    ClientUtils.syncFullMoonConfig(pos, config);
                }
            }
        };
    }

    public static @Nullable LampTarget heldLampItem(Minecraft mc, InteractionHand hand) {
        if (mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!stack.is(ModItems.MOONLIGHT_LAMP.get())) {
            return null;
        }
        return new LampTarget() {
            @Override
            public LampDeviceConfig get() {
                ItemStack current = mc.player.getItemInHand(hand);
                return current.getOrDefault(ModDataComponent.LAMP_CONFIG.get(), LampDeviceConfig.fromGlobalConfig());
            }

            @Override
            public void apply(LampDeviceConfig config) {
                ItemStack current = mc.player.getItemInHand(hand);
                if (!current.is(ModItems.MOONLIGHT_LAMP.get())) {
                    return;
                }
                current.set(ModDataComponent.LAMP_CONFIG.get(), config);
                ClientUtils.syncHeldMoonlightLampConfig(hand, config);
            }
        };
    }

    public static @Nullable FullMoonTarget heldFullMoonItem(Minecraft mc, InteractionHand hand) {
        if (mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!stack.is(ModItems.FULL_MOON.get())) {
            return null;
        }
        return new FullMoonTarget() {
            @Override
            public FullMoonDeviceConfig get() {
                ItemStack current = mc.player.getItemInHand(hand);
                return current.getOrDefault(ModDataComponent.FULL_MOON_CONFIG.get(), FullMoonDeviceConfig.fromGlobalConfig());
            }

            @Override
            public void apply(FullMoonDeviceConfig config) {
                ItemStack current = mc.player.getItemInHand(hand);
                if (!current.is(ModItems.FULL_MOON.get())) {
                    return;
                }
                current.set(ModDataComponent.FULL_MOON_CONFIG.get(), config);
                ClientUtils.syncHeldFullMoonConfig(hand, config);
            }
        };
    }
}
