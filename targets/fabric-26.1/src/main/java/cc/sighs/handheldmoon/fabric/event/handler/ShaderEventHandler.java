package cc.sighs.handheldmoon.fabric.event.handler;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public final class ShaderEventHandler {
    private static float previousYaw;
    private static float previousPitch;
    private static float offsetX;
    private static float offsetY;
    private static long lastTickTime = System.currentTimeMillis();

    private ShaderEventHandler() {
    }

    public static void onClientTick() {
        if (Config.LIGHT_INTENSITY.get() < 0.1) {
            EffectManager.clean("flashlight");
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            EffectManager.clean("flashlight");
            return;
        }

        Player player = mc.player;
        if (player == null) {
            EffectManager.clean("flashlight");
            return;
        }

        if (Utils.isUsingFlashlight(player)) {
            EffectManager.loadEffect("flashlight", "post_effect/flashlight.json");

            long now = System.currentTimeMillis();
            float deltaSeconds = Math.max((now - lastTickTime) / 1000.0f, 0.001f);
            lastTickTime = now;
            float deltaYaw = Mth.wrapDegrees(player.getYRot() - previousYaw);
            float deltaPitch = Mth.wrapDegrees(player.getXRot() - previousPitch);
            previousYaw = player.getYRot();
            previousPitch = player.getXRot();
            offsetX = offsetX * 0.5f - deltaYaw * 70.0f * deltaSeconds;
            offsetY = offsetY * 0.5f - deltaPitch * 70.0f * deltaSeconds;
            if (Config.ENABLE_FIXED_FLASHLIGHT.get()) {
                offsetX = 0.0f;
                offsetY = 0.0f;
            }

            float radius = mc.getWindow().getHeight() * 0.48f;
            if (mc.options.getCameraType() != CameraType.FIRST_PERSON) {
                radius /= 2.0f;
            }
            float ratio = radius / Math.min(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            EffectManager.updateFlashlightUniforms(offsetX, -offsetY, ratio,
                    Config.LIGHT_INTENSITY.get().floatValue());
        } else {
            EffectManager.clean("flashlight");
            offsetX = offsetY = 0.0f;
            previousYaw = player.getYRot();
            previousPitch = player.getXRot();
        }
    }
}

