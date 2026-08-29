package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess;
import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
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
    private DeviceConfigTargets() {
    }

    public static @Nullable ConfigTarget<LampDeviceConfig> cursorLampBlock(Minecraft mc) {
        var lamp = ClientUtils.getCursorMoonlightLampBlock();
        if (lamp == null) {
            return null;
        }
        BlockPos pos = lamp.getBlockPos();
        return new ConfigTarget<LampDeviceConfig>() {
            @Override
            public LampDeviceConfig get() {
                if (mc.level == null) {
                    return LampDeviceConfig.fromGlobalConfig();
                }
                if (mc.level.getBlockEntity(pos) instanceof MoonlightLampBlockEntityAccess target) {
                    return target.getLampConfig();
                }
                return LampDeviceConfig.fromGlobalConfig();
            }

            @Override
            public void apply(LampDeviceConfig config) {
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof MoonlightLampBlockEntityAccess target) {
                    target.setLampConfig(config, true);
                    ClientUtils.syncMoonlightLampConfig(pos, config);
                }
            }
        };
    }

    public static @Nullable ConfigTarget<FullMoonDeviceConfig> cursorFullMoonBlock(Minecraft mc) {
        var moon = ClientUtils.getCursorFullMoonBlock();
        if (moon == null) {
            return null;
        }
        BlockPos pos = moon.getBlockPos();
        return new ConfigTarget<FullMoonDeviceConfig>() {
            @Override
            public FullMoonDeviceConfig get() {
                if (mc.level == null) {
                    return FullMoonDeviceConfig.fromGlobalConfig();
                }
                if (mc.level.getBlockEntity(pos) instanceof FullMoonBlockEntityAccess target) {
                    return target.getFullMoonConfig();
                }
                return FullMoonDeviceConfig.fromGlobalConfig();
            }

            @Override
            public void apply(FullMoonDeviceConfig config) {
                if (mc.level != null && mc.level.getBlockEntity(pos) instanceof FullMoonBlockEntityAccess target) {
                    target.setFullMoonConfig(config, true);
                    ClientUtils.syncFullMoonConfig(pos, config);
                }
            }
        };
    }

    public static @Nullable ConfigTarget<LampDeviceConfig> heldLampItem(Minecraft mc, InteractionHand hand) {
        if (mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!stack.is(ModItems.MOONLIGHT_LAMP.get())) {
            return null;
        }
        return new ConfigTarget<LampDeviceConfig>() {
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

    public static @Nullable ConfigTarget<FullMoonDeviceConfig> heldFullMoonItem(Minecraft mc, InteractionHand hand) {
        if (mc.player == null) {
            return null;
        }
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!stack.is(ModItems.FULL_MOON.get())) {
            return null;
        }
        return new ConfigTarget<FullMoonDeviceConfig>() {
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
