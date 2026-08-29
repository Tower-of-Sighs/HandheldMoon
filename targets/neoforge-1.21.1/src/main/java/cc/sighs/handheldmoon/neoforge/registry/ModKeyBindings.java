package cc.sighs.handheldmoon.neoforge.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Minecraft 1.21.1 key mapping adapter. */
public final class ModKeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping(
            "key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.handheldmoon"
    );
    public static final KeyMapping OPEN_DEVICE_CONFIG = new KeyMapping(
            "key.handheldmoon.device_config.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.handheldmoon"
    );

    private ModKeyBindings() {
    }
}
