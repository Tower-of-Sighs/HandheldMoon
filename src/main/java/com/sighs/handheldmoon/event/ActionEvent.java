package com.sighs.handheldmoon.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.KeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class ActionEvent {
    private static double cacheGama = 0;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (event.getKey() == KeyBindings.FLASHLIGHT_SWITCH.getKey().getValue()) {
            if (event.getAction() == InputConstants.PRESS) {
                cacheGama = Config.LIGHT_INTENSITY.get();
            }
            if (event.getAction() == InputConstants.RELEASE) {
                if (cacheGama == Config.LIGHT_INTENSITY.get()) {
                    Utils.toggleFlashlight(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void wheelAction(net.minecraftforge.client.event.InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !Utils.isUsingFlashlight(player)) return;
        if (KeyBindings.FLASHLIGHT_SWITCH.isDown()) {
            modifyValue(event.getScrollDelta());
            event.setCanceled(true);
        }
    }

    public static void modifyValue(double delta) {
        double value = Config.LIGHT_INTENSITY.get();
        if (delta < 0) {
            value = Math.max(value - 0.1, 0.1);
        } else {
            value = Math.min(value + 0.1, 1);
        }
        value = Math.round(value * 10) / 10d;
        Config.LIGHT_INTENSITY.set(value);
        Config.LIGHT_INTENSITY.save();
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.handheldmoon.light_tweak", String.valueOf(value).substring(0, 3)), true);
    }
}
