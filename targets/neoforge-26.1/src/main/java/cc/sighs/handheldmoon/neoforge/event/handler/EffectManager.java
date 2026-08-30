package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.PostPass;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import cc.sighs.handheldmoon.neoforge.mixin.client.PostPassAccessor;
import cc.sighs.handheldmoon.neoforge.mixin.client.GameRendererAccessor;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class EffectManager {
    private static final String FLASHLIGHT_NAME = "flashlight";
    private static final String FLASHLIGHT_UNIFORM_NAME = "FlashlightParams";
    private static final int FLASHLIGHT_UNIFORM_SIZE = 32;
    private static final Identifier FLASHLIGHT_EFFECT_ID = Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, FLASHLIGHT_NAME);
    private static boolean flashlightEnabled = false;
    private static final Map<String, String> EFFECT_PATHS = new ConcurrentHashMap<>();
    private static float offsetX;
    private static float offsetY;
    private static float radiusRatio = 0.48f;
    private static float intensity = 1.0f;
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
        if (flashlightEnabled) EFFECT_PATHS.put(name, ignoredPath);
        return flashlightEnabled;
    }

    public static void clean(String name) {
        if (FLASHLIGHT_NAME.equals(name)) {
            flashlightEnabled = false;
            EFFECT_PATHS.remove(name);
        }
    }

    public static List<PostPass> getEffect(String name) {
        if (!FLASHLIGHT_NAME.equals(name) || !flashlightEnabled) return Collections.emptyList();
        PostChain chain = Minecraft.getInstance().getShaderManager()
                .getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
        return chain == null ? Collections.emptyList() : chain.passes;
    }

    public static boolean isLoading(String name) {
        return EFFECT_PATHS.containsKey(name);
    }

    public static boolean isValid(String name) {
        return FLASHLIGHT_NAME.equals(name) && Minecraft.getInstance().getShaderManager()
                .getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS) != null;
    }

    public static void initAll() {
    }

    public static void setEffectEnabled(String name, boolean enabled) {
        if (FLASHLIGHT_NAME.equals(name)) flashlightEnabled = enabled;
    }

    public static boolean isEffectEnabled(String name) {
        return FLASHLIGHT_NAME.equals(name) && flashlightEnabled;
    }

    public static Set<String> getLoadedEffectNames() {
        return Collections.unmodifiableSet(EFFECT_PATHS.keySet());
    }

    public static int getActiveEffectCount() {
        return flashlightEnabled ? 1 : 0;
    }

    public static void cleanup() {
        flashlightEnabled = false;
        EFFECT_PATHS.clear();
        if (flashlightUniformBuffer != null && !flashlightUniformBuffer.isClosed()) {
            flashlightUniformBuffer.close();
        }
        flashlightUniformBuffer = null;
    }

    /** Updates the 26.1 std140 uniform block used by the flashlight pass. */
    public static void updateFlashlightUniforms(float newOffsetX, float newOffsetY,
                                                float newRadiusRatio, float newIntensity) {
        offsetX = newOffsetX;
        offsetY = newOffsetY;
        radiusRatio = newRadiusRatio;
        intensity = newIntensity;
        if (!flashlightEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        PostChain chain = mc.getShaderManager().getPostChain(FLASHLIGHT_EFFECT_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null || chain.passes.isEmpty()) return;
        PostPass pass = chain.passes.get(0);
        Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).handheldmoon$getCustomUniforms();
        GpuBuffer previous = uniforms.get(FLASHLIGHT_UNIFORM_NAME);
        if (previous == null) return;

        if (flashlightUniformBuffer == null || flashlightUniformBuffer.isClosed()
                || flashlightUniformBuffer != previous) {
            GpuBuffer oldManagedBuffer = flashlightUniformBuffer;
            flashlightUniformBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "HandheldMoon flashlight uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_COPY_DST,
                    FLASHLIGHT_UNIFORM_SIZE);
            uniforms.put(FLASHLIGHT_UNIFORM_NAME, flashlightUniformBuffer);

            // PostPass creates an immutable buffer for values from JSON. It is no longer
            // referenced once the writable replacement is installed.
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
