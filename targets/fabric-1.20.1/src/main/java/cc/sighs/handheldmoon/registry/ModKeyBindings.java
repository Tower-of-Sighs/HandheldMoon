package cc.sighs.handheldmoon.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/** Minecraft 1.20.1 key mapping adapter. */
public final class ModKeyBindings {
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping(
            "key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.handheldmoon"
    );
    public static final KeyMapping OPEN_MOD_CONFIG = new KeyMapping(
            "key.handheldmoon.mod_config.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.handheldmoon"
    );
    public static final KeyMapping CONE_SWITCH = new KeyMapping(
            "key.handheldmoon.cone_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "key.categories.handheldmoon"
    );

    private ModKeyBindings() {
    }
}
