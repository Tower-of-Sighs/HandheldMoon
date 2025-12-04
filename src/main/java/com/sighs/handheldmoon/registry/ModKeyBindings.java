package com.sighs.handheldmoon.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.handheldmoon.HandheldMoon;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.handheldmoon"
    );
}
