package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.handheldmoon.event.InputEvent;
import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import com.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class OperationEventHandler {
    private static double cacheGama = 0;
    private static long lastActionTime;

    public static void init() {
        InputEvent.KEY.register(OperationEventHandler::onKey);
        InputEvent.MOUSE_BUTTON_PRE.register(OperationEventHandler::wheelAction);
        InputEvent.MOUSE_SCROLL.register(OperationEventHandler::wheelAction);
    }

    public static void onKey(int key, int scancode, int action, int modifiers) {
        if (Minecraft.getInstance().screen != null) return;
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        if (key == ModKeyBindings.FLASHLIGHT_SWITCH.key.getValue()) {
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

    public static boolean wheelAction(int button, int action, int modifiers) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        if (ModKeyBindings.FLASHLIGHT_SWITCH.isDown() && action == InputConstants.RELEASE) {
            if (player.tickCount - lastActionTime < 10) return false;
            if (button == 0) {
                Config.REAL_LIGHT.set(!Config.REAL_LIGHT.get());
                Config.REAL_LIGHT.save();
                player.displayClientMessage(Component.translatable("message.handheldmoon.real_light", Config.REAL_LIGHT.get().toString()), true);
                return true;
            }
            if (button == 1) {
                Config.PLAYER_RAY.set(!Config.PLAYER_RAY.get());
                Config.PLAYER_RAY.save();
                player.displayClientMessage(Component.translatable("message.handheldmoon.player_ray", Config.PLAYER_RAY.get().toString()), true);
                return true;
            }
            lastActionTime = player.tickCount;
        }
        return false;
    }

    public static boolean wheelAction(double scrollDeltaX, double scrollDeltaY, double mouseX, double mouseY,
                                      boolean leftDown, boolean middleDown, boolean rightDown) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !Utils.isUsingFlashlight(player)) return false;
        if (ModKeyBindings.FLASHLIGHT_SWITCH.isDown()) {
            modifyValue(scrollDeltaY);
            return true;
        }
        return false;
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
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("message.handheldmoon.light_tweak", String.valueOf(value).substring(0, 3)), true);
    }
}