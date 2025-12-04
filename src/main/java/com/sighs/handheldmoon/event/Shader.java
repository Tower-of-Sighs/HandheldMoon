package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.init.EffectManager;
import com.sighs.handheldmoon.init.Utils;
import com.sighs.handheldmoon.registry.Config;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class Shader {
    private static float previousYaw = 0.0f;
    private static float previousPitch = 0.0f;
    private static float currentOffsetX = 0.0f;
    private static float currentOffsetY = 0.0f;
    private static long lastTickTime = System.currentTimeMillis();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (Config.LIGHT_INTENSITY.get() < 0.1) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT) return;
        Player player = mc.player;
        if (player == null) return;

        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastTickTime;
        float deltaSeconds = Math.max((deltaTime / 1000.0f), 0.001f);
        lastTickTime = currentTime;

        if (Utils.isUsingFlashlight(player)) {
            EffectManager.loadEffect("flashlight", "shaders/post/flashlight.json");

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

            if (mc.options.getCameraType() == CameraType.THIRD_PERSON_FRONT || Config.ENABLE_FIXED_FLASHLIGHT.get()) {
                currentOffsetX = 0;
                currentOffsetY = 0;
            }

            float radius = mc.getWindow().getHeight() * 0.48f;
            if (mc.options.getCameraType() != CameraType.FIRST_PERSON) radius /= 2;
            float finalRadius = radius;

            EffectManager.getEffect("flashlight").forEach(postPass -> {
                EffectInstance effect = postPass.getEffect();
                if (effect.getName().equals("handheldmoon:flashlight")) {
                    effect.safeGetUniform("Offset").set(currentOffsetX, -currentOffsetY);
                    effect.safeGetUniform("Radius").set(finalRadius);
                    effect.safeGetUniform("IntensityAmount").set(Config.LIGHT_INTENSITY.get().floatValue());
                }
            });
        } else {
            EffectManager.clean("flashlight");

            currentOffsetX = 0.0f;
            currentOffsetY = 0.0f;
            previousYaw = player.getYRot();
            previousPitch = player.getXRot();
        }
    }
}
