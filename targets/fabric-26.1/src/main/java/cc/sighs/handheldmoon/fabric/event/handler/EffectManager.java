package cc.sighs.handheldmoon.fabric.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.Identifier;
import cc.sighs.handheldmoon.fabric.mixin.PostPassAccessor;
import cc.sighs.handheldmoon.fabric.mixin.PostChainAccessor;
import cc.sighs.handheldmoon.fabric.mixin.GameRendererAccessor;

import java.nio.ByteBuffer;
import java.util.Map;

public final class EffectManager {
    private static final String FLASHLIGHT_NAME = "flashlight";
    private static final String FLASHLIGHT_UNIFORM_NAME = "FlashlightParams";
    private static final int FLASHLIGHT_UNIFORM_SIZE = 32;
    private static final Identifier FLASHLIGHT_EFFECT_ID = Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, FLASHLIGHT_NAME);
    private static boolean flashlightEnabled = false;
    private static GpuBuffer flashlightUniformBuffer;

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

    /** Updates the 26.1 std140 uniform block used by the flashlight pass. */
    public static void updateFlashlightUniforms(float offsetX, float offsetY,
                                                float radiusRatio, float intensity) {
        if (!flashlightEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        PostChain chain = mc.getShaderManager().getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null || ((PostChainAccessor) chain).handheldmoon$getPasses().isEmpty()) {
            return;
        }

        PostPass pass = ((PostChainAccessor) chain).handheldmoon$getPasses().get(0);
        Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).handheldmoon$getCustomUniforms();
        GpuBuffer previous = uniforms.get(FLASHLIGHT_UNIFORM_NAME);
        if (previous == null) {
            return;
        }

        if (flashlightUniformBuffer == null || flashlightUniformBuffer.isClosed()
                || flashlightUniformBuffer != previous) {
            GpuBuffer oldManagedBuffer = flashlightUniformBuffer;
            flashlightUniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "HandheldMoon flashlight uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                    FLASHLIGHT_UNIFORM_SIZE);
            uniforms.put(FLASHLIGHT_UNIFORM_NAME, flashlightUniformBuffer);

            if (oldManagedBuffer != null && oldManagedBuffer != previous && !oldManagedBuffer.isClosed()) {
                oldManagedBuffer.close();
            }
            if (previous != flashlightUniformBuffer && !previous.isClosed()) {
                previous.close();
            }
        }

        try (GpuBuffer.MappedView view = RenderSystem.getDevice().createCommandEncoder()
                .mapBuffer(flashlightUniformBuffer, false, true)) {
            ByteBuffer data = view.data();
            Std140Builder builder = Std140Builder.intoBuffer(data);
            builder.putFloat(intensity).align(8).putVec2(offsetX, offsetY).putFloat(radiusRatio);
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
            CrossFrameResourcePool resourcePool = ((GameRendererAccessor) mc.gameRenderer)
                    .handheldmoon$getResourcePool();
            chain.process(mc.getMainRenderTarget(), resourcePool);
        } catch (IllegalStateException ex) {
            flashlightEnabled = false;
            HandheldMoon.LOGGER.error("Failed to process flashlight post effect, disabling it for this session.", ex);
        }
    }
}
