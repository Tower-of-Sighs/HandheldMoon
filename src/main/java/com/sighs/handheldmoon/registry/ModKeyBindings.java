package com.sighs.handheldmoon.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.handheldmoon"
    );
}
