package cc.sighs.handheldmoon.neoforge.registry;

import cc.sighs.handheldmoon.HandheldMoon;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class ModKeyBindings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(HandheldMoon.id("main"));
    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping(
            "key.handheldmoon.lamp_switch.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    public static final KeyMapping CONE_SWITCH = new KeyMapping(
            "key.handheldmoon.cone_switch.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, CATEGORY);
    public static final KeyMapping OPEN_MOD_CONFIG = new KeyMapping(
            "key.handheldmoon.mod_config.desc", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private ModKeyBindings() {
    }
}
