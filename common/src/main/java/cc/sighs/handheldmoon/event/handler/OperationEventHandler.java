package cc.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.InputConstants;
import cc.sighs.handheldmoon.client.screen.DeviceConfigOpenHelper;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.spi.PlatformServices;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class OperationEventHandler {
    private static double cacheGama = 0;
    private static long lastActionTime;
    private static boolean vComboUsed;

    private OperationEventHandler() {
    }

    public static void onKey(int key, int action) {
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        var keys = PlatformServices.require().client();
        if (key == keys.deviceConfigKeyCode() && action == InputConstants.PRESS) {
            DeviceConfigOpenHelper.openForCursorTarget();
            return;
        }
        if (key == keys.flashlightKeyCode()) {
            if (action == InputConstants.PRESS) {
                cacheGama = Config.LIGHT_INTENSITY.get();
                vComboUsed = false;
            }
            if (action == InputConstants.RELEASE) {
                if (!vComboUsed && cacheGama == Config.LIGHT_INTENSITY.get()) {
                    Utils.toggleFlashlight(player);
                }
            }
        }
    }

    public static boolean onMouseButton(int button, int action) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !PlatformServices.require().client().isFlashlightKeyDown()) {
            return false;
        }

        if (action == InputConstants.PRESS) {
            vComboUsed = true;
            return true;
        }
        if (action == InputConstants.RELEASE) {
            if (player.tickCount - lastActionTime < 10) {
                return true;
            }
            if (button == 0) {
                Config.REAL_LIGHT.set(!Config.REAL_LIGHT.get());
                Config.REAL_LIGHT.save();
                player.sendSystemMessage(Component.translatable("message.handheldmoon.real_light", Config.REAL_LIGHT.get().toString()));
                vComboUsed = true;
            }
            if (button == 1) {
                Config.PLAYER_RAY.set(!Config.PLAYER_RAY.get());
                Config.PLAYER_RAY.save();
                player.sendSystemMessage(Component.translatable("message.handheldmoon.player_ray", Config.PLAYER_RAY.get().toString()));
                vComboUsed = true;
            }
            lastActionTime = player.tickCount;
            return true;
        }
        return false;
    }

    public static boolean onMouseScroll(double deltaY) {
        Player player = Minecraft.getInstance().player;
        if (player == null || !Utils.isUsingFlashlight(player)) {
            return false;
        }
        if (PlatformServices.require().client().isFlashlightKeyDown()) {
            vComboUsed = true;
            modifyValue(deltaY);
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
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.handheldmoon.light_tweak", String.format("%.1f", value)));
        }
    }
}
