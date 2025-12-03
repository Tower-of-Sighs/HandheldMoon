package com.sighs.handheldmoon.registry;

import com.mojang.blaze3d.platform.InputConstants;
import com.sighs.handheldmoon.HandheldMoon;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyMapping.Category HANDHELD_MOON = KeyMapping.Category.register(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "handheld_moon"));
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            HANDHELD_MOON
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(FLASHLIGHT_SWITCH);
    }
}
