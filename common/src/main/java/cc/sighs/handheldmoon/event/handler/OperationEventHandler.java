package cc.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.platform.InputConstants;
import cc.sighs.handheldmoon.api.config.ConfigTarget;
import cc.sighs.handheldmoon.client.screen.DeviceConfigTargets;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.spi.PlatformServices;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

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
        if (key == keys.modConfigKeyCode() && action == InputConstants.PRESS) {
            keys.openModConfig();
            return;
        }
        if (key == keys.coneSwitchKeyCode() && action == InputConstants.PRESS) {
            toggleHeldVisibleCone(player);
            return;
        }
        if (key == keys.flashlightKeyCode()) {
            if (action == InputConstants.PRESS) {
                cacheGama = heldFilterIntensity(player);
                vComboUsed = false;
            }
            if (action == InputConstants.RELEASE) {
                if (!vComboUsed && cacheGama == heldFilterIntensity(player)) {
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
                toggleHeldRealLight(player);
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
        ConfigTarget<LampDeviceConfig> target = heldLampTarget();
        if (target == null) {
            return;
        }
        LampDeviceConfig config = target.get();
        double value = config.filterLightIntensity();
        if (delta < 0) {
            value = Math.max(value - 0.1, 0);
        } else {
            value = Math.min(value + 0.1, 1);
        }
        value = Math.round(value * 10) / 10d;
        target.apply(config.withFilterLightIntensity(value));
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.handheldmoon.light_tweak", String.format("%.1f", value)));
        }
    }

    /** Toggles the visible cone of the held flashlight item, not a global switch. */
    private static void toggleHeldVisibleCone(Player player) {
        ConfigTarget<LampDeviceConfig> target = heldLampTarget();
        if (target == null) {
            return;
        }
        LampDeviceConfig config = target.get();
        boolean enabled = !config.enableVisibleCone();
        target.apply(config.withEnableVisibleCone(enabled));
        player.sendSystemMessage(Component.translatable(
                "message.handheldmoon.visible_cone",
                Component.translatable(enabled ? "options.on" : "options.off")));
    }

    /** Toggles the held flashlight's real light between off and full intensity. */
    private static void toggleHeldRealLight(Player player) {
        ConfigTarget<LampDeviceConfig> target = heldLampTarget();
        if (target == null) {
            return;
        }
        LampDeviceConfig config = target.get();
        boolean enabled = config.realLightIntensity() <= 0;
        double intensity = enabled ? LampDeviceConfig.DEFAULT_REAL_LIGHT_INTENSITY : 0.0;
        target.apply(config.withRealLightIntensity(intensity));
        player.sendSystemMessage(Component.translatable(
                "message.handheldmoon.real_light",
                Component.translatable(enabled ? "options.on" : "options.off")));
    }

    private static double heldFilterIntensity(Player player) {
        LampDeviceConfig config = Utils.heldLampConfig(player);
        return config != null ? config.filterLightIntensity() : LampDeviceConfig.DEFAULT_FILTER_LIGHT_INTENSITY;
    }

    private static @Nullable ConfigTarget<LampDeviceConfig> heldLampTarget() {
        Minecraft minecraft = Minecraft.getInstance();
        ConfigTarget<LampDeviceConfig> target = DeviceConfigTargets.heldLampItem(minecraft, InteractionHand.MAIN_HAND);
        return target != null ? target : DeviceConfigTargets.heldLampItem(minecraft, InteractionHand.OFF_HAND);
    }
}
