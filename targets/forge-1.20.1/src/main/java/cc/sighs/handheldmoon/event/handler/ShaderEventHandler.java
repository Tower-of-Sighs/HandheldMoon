package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Drives the flashlight post effect: feeds the legacy {@code flashlight}
 * program its offset, radius ratio and intensity every client tick.
 */
public final class ShaderEventHandler {
    private static final String EFFECT_NAME = "flashlight";
    private static final String EFFECT_PATH = "shaders/post/flashlight.json";

    private static float previousYaw = 0.0f;
    private static float previousPitch = 0.0f;
    private static float currentOffsetX = 0.0f;
    private static float currentOffsetY = 0.0f;
    private static long lastTickTime = System.currentTimeMillis();

    private ShaderEventHandler() {
    }

    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        float filterIntensity = player == null ? 1.0f
                : (float) Utils.heldLampConfigOrDefault(player).filterLightIntensity();
        if (filterIntensity < 0.1) {
            EffectManager.clean(EFFECT_NAME);
            return;
        }
        if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) {
            disableFlashlight();
            return;
        }
        if (player == null) {
            disableFlashlight();
            return;
        }

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTickTime;
        float deltaSeconds = Math.max((deltaTime / 1000.0f), 0.001f);
        lastTickTime = currentTime;

        if (Utils.isUsingFlashlight(player)) {
            EffectManager.loadEffect(EFFECT_NAME, EFFECT_PATH);

            // 视角变化量
            float currentYaw = player.getYRot();
            float currentPitch = player.getXRot();
            float deltaYaw = Mth.wrapDegrees(currentYaw - previousYaw);
            float deltaPitch = Mth.wrapDegrees(currentPitch - previousPitch);
            previousYaw = currentYaw;
            previousPitch = currentPitch;

            float sensitivity = 70.0f;
            // 反向偏移
            float offsetDeltaX = -deltaYaw * sensitivity * deltaSeconds;
            float offsetDeltaY = -deltaPitch * sensitivity * deltaSeconds;

            // 衰减
            currentOffsetX = currentOffsetX * 0.5f + offsetDeltaX;
            currentOffsetY = currentOffsetY * 0.5f + offsetDeltaY;

            if (Config.ENABLE_FIXED_FLASHLIGHT.get()) {
                currentOffsetX = 0;
                currentOffsetY = 0;
            }

            float radius = mc.getWindow().getHeight() * 0.48f;
            if (mc.options.getCameraType() != CameraType.FIRST_PERSON) radius /= 2;
            float radiusRatio = radius / Math.min(mc.getWindow().getWidth(), mc.getWindow().getHeight());

            for (PostPass pass : EffectManager.getEffect(EFFECT_NAME)) {
                EffectInstance effect = pass.getEffect();
                if (!isFlashlightProgram(effect.getName())) {
                    continue;
                }
                effect.safeGetUniform("Offset").set(currentOffsetX, -currentOffsetY);
                effect.safeGetUniform("RadiusRatio").set(radiusRatio);
                effect.safeGetUniform("IntensityAmount").set(filterIntensity);
            }
        } else {
            disableFlashlight();
            previousYaw = player.getYRot();
            previousPitch = player.getXRot();
        }
    }

    private static boolean isFlashlightProgram(String name) {
        return EFFECT_NAME.equals(name) || (name != null && name.endsWith(":" + EFFECT_NAME));
    }

    private static void disableFlashlight() {
        EffectManager.clean(EFFECT_NAME);
        currentOffsetX = 0.0f;
        currentOffsetY = 0.0f;
    }
}
