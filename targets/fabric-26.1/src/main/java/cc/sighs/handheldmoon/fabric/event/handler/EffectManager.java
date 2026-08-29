package cc.sighs.handheldmoon.fabric.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;

public final class EffectManager {
    private static final String FLASHLIGHT_NAME = "flashlight";
    private static final Identifier FLASHLIGHT_EFFECT_ID = Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, FLASHLIGHT_NAME);
    private static boolean flashlightEnabled = false;

    private EffectManager() {
    }

    public static boolean loadEffect(String name, String ignoredPath) {
        if (!FLASHLIGHT_NAME.equals(name)) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        PostChain chain = mc.getShaderManager().getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
        flashlightEnabled = chain != null;
        return flashlightEnabled;
    }

    public static void clean(String name) {
        if (FLASHLIGHT_NAME.equals(name)) {
            flashlightEnabled = false;
        }
    }

    public static void onLevelRender() {
        if (!flashlightEnabled) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        PostChain chain = mc.getShaderManager().getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            flashlightEnabled = false;
            return;
        }
        try {
            chain.process(mc.getMainRenderTarget(), GraphicsResourceAllocator.UNPOOLED);
        } catch (IllegalStateException ex) {
            flashlightEnabled = false;
            HandheldMoon.LOGGER.error("Failed to process flashlight post effect, disabling it for this session.", ex);
        }
    }
}


