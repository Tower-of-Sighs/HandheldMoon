package cc.sighs.handheldmoon.client.screen;

import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.item.ItemData;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Config targets used by the in-game flashlight shortcuts. */
public final class DeviceConfigTargets {
    private DeviceConfigTargets() {
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
                return ItemData.lampConfig(current);
            }

            @Override
            public void apply(LampDeviceConfig config) {
                ItemStack current = mc.player.getItemInHand(hand);
                if (!current.is(ModItems.MOONLIGHT_LAMP.get())) {
                    return;
                }
                ItemData.setLampConfig(current, config);
                ClientUtils.syncHeldMoonlightLampConfig(hand, config);
            }
        };
    }
}
