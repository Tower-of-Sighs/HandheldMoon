package cc.sighs.handheldmoon.fabric.event.handler;

import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ShaderEventHandler {
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
        } else {
            EffectManager.clean("flashlight");
        }
    }
}


