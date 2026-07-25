package cc.sighs.handheldmoon.registry;

import cc.sighs.oelib.registry.extra.KeyMappingRegister;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("handheldmoon", "main"));

    public static final KeyMapping FLASHLIGHT_SWITCH = new KeyMapping("key.handheldmoon.lamp_switch.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    public static final KeyMapping OPEN_DEVICE_CONFIG = new KeyMapping("key.handheldmoon.device_config.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static void register() {
        KeyMappingRegister.register(FLASHLIGHT_SWITCH);
        KeyMappingRegister.register(OPEN_DEVICE_CONFIG);
    }
}
