package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.handheldmoon.Config;
import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class OperationEventHandler {

    private static double cacheGama = 0;
    private static long lastActionTime;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (Minecraft.getInstance().screen != null) return;

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 获取事件参数
        int key = event.getKey();
        int action = event.getAction();

        if (key == ModKeyBindings.FLASHLIGHT_SWITCH.getKey().getValue()) {
            if (action == InputConstants.PRESS) {
                cacheGama = Config.LIGHT_INTENSITY.get();
            }
            if (action == InputConstants.RELEASE) {
                if (cacheGama == Config.LIGHT_INTENSITY.get()) {
                    Utils.toggleFlashlight(player);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int button = event.getButton();
        int action = event.getAction();

        if (ModKeyBindings.FLASHLIGHT_SWITCH.isDown() && action == InputConstants.RELEASE) {
            if (player.tickCount - lastActionTime < 10) return;

            boolean handled = false;

            if (button == 0) { // 左键
                Config.REAL_LIGHT.set(!Config.REAL_LIGHT.get());
                Config.REAL_LIGHT.save();
                player.displayClientMessage(Component.translatable("message.handheldmoon.real_light", Config.REAL_LIGHT.get().toString()), true);
                handled = true;
            } else if (button == 1) { // 右键
                Config.PLAYER_RAY.set(!Config.PLAYER_RAY.get());
                Config.PLAYER_RAY.save();
                player.displayClientMessage(Component.translatable("message.handheldmoon.player_ray", Config.PLAYER_RAY.get().toString()), true);
                handled = true;
            }

            if (handled) {
                lastActionTime = player.tickCount;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !Utils.isUsingFlashlight(player)) return;

        if (ModKeyBindings.FLASHLIGHT_SWITCH.isDown()) {
            double scrollDeltaY = event.getScrollDeltaY();

            modifyValue(scrollDeltaY);
            event.setCanceled(true);
        }
    }

    public static void modifyValue(double delta) {
        double value = Config.LIGHT_INTENSITY.get();
        if (delta < 0) {
            value = Math.max(value - 0.1, 0);
        } else {
            value = Math.min(value + 0.1, 1);
        }
        value = Math.round(value * 10) / 10d;
        Config.LIGHT_INTENSITY.set(value);
        Config.LIGHT_INTENSITY.save();
        // 这里的 player 肯定不为 null，因为是从 onMouseScroll 调用的
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.handheldmoon.light_tweak", String.valueOf(value).substring(0, 3)), true);
        }
    }
}