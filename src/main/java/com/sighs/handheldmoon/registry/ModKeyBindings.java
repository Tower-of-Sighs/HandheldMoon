package com.sighs.handheldmoon.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.handheldmoon"
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(FLASHLIGHT_SWITCH);
    }
}
