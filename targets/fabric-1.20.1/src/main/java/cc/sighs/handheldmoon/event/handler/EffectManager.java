package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.mixin.client.PostChainAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the 1.20.1 {@link PostChain} instances built from legacy
 * {@code shaders/post/*.json} descriptors.
 */
public final class EffectManager {
    private static final Map<String, ShaderEffect> EFFECTS = new ConcurrentHashMap<>();
    private static final Set<String> REPORTED_FAILURES = ConcurrentHashMap.newKeySet();
    private static int lastWindowWidth = -1;
    private static int lastWindowHeight = -1;

    private EffectManager() {
    }

    private static final class ShaderEffect {
        private final String jsonPath;
        private PostChain postChain;
        private boolean enabled = true;
        private boolean needsResize = true;

        private ShaderEffect(String jsonPath) {
            this.jsonPath = jsonPath;
            this.postChain = createPostChain(jsonPath);
        }

        private static PostChain createPostChain(String path) {
            ResourceLocation location = new ResourceLocation(HandheldMoon.MOD_ID, path);
            Minecraft minecraft = Minecraft.getInstance();
            try {
                return new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(),
                        minecraft.getMainRenderTarget(), location);
            } catch (IOException e) {
                if (REPORTED_FAILURES.add(path)) {
                    HandheldMoon.LOGGER.error("Failed to load post chain {}", location, e);
                }
                return null;
            }
        }

        private List<PostPass> getPasses() {
            return postChain != null ? ((PostChainAccessor) postChain).handheldmoon$getPasses()
                    : Collections.emptyList();
        }

        private boolean isValid() {
            return postChain != null && !getPasses().isEmpty();
        }

        private void resize(int width, int height) {
            if (postChain != null && needsResize) {
                postChain.resize(width, height);
                needsResize = false;
            }
        }

        private void process(float partialTick) {
            if (postChain != null && enabled) {
                postChain.process(partialTick);
            }
        }

        private void close() {
            if (postChain != null) {
                postChain.close();
                postChain = null;
            }
        }

        private void markForResize() {
            this.needsResize = true;
        }

        private void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        private boolean isEnabled() {
            return enabled;
        }
    }

    /** Runs on every client resource reload; the resource manager hands off to the client thread. */
    public static void onResourceReload() {
        Minecraft.getInstance().execute(EffectManager::reloadAllShaders);
    }

    private static void reloadAllShaders() {
        EFFECTS.values().forEach(ShaderEffect::close);

        EFFECTS.replaceAll((name, oldEffect) -> {
            ShaderEffect newEffect = new ShaderEffect(oldEffect.jsonPath);
            if (!newEffect.isValid()) {
                return oldEffect;
            }
            return newEffect;
        });

        markAllForResize();
    }

    public static List<PostPass> getEffect(String name) {
        ShaderEffect effect = EFFECTS.get(name);
        return effect != null ? effect.getPasses() : Collections.emptyList();
    }

    public static boolean loadEffect(String name, String jsonPath) {
        ShaderEffect effect = EFFECTS.get(name);
        if (effect != null) {
            return effect.isValid();
        }
        ShaderEffect created = new ShaderEffect(jsonPath);
        if (created.isValid()) {
            EFFECTS.put(name, created);
            return true;
        }
        return false;
    }

    public static boolean isLoading(String name) {
        return EFFECTS.containsKey(name);
    }

    public static boolean isValid(String name) {
        ShaderEffect effect = EFFECTS.get(name);
        return effect != null && effect.isValid();
    }

    public static void initAll() {
        markAllForResize();
    }

    /** Processes every loaded effect after the level has been drawn. */
    public static void onRenderLevelStage() {
        checkAndHandleResize();
        float partialTick = Minecraft.getInstance().getFrameTime();
        EFFECTS.values().forEach(effect -> effect.process(partialTick));
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    private static void checkAndHandleResize() {
        Minecraft minecraft = Minecraft.getInstance();
        int currentWidth = minecraft.getWindow().getWidth();
        int currentHeight = minecraft.getWindow().getHeight();

        if (currentWidth != lastWindowWidth || currentHeight != lastWindowHeight) {
            lastWindowWidth = currentWidth;
            lastWindowHeight = currentHeight;
            markAllForResize();
        }

        if (currentWidth > 0 && currentHeight > 0) {
            EFFECTS.values().forEach(effect -> effect.resize(currentWidth, currentHeight));
        }
    }

    private static void markAllForResize() {
        EFFECTS.values().forEach(ShaderEffect::markForResize);
    }

    public static void clean(String name) {
        ShaderEffect effect = EFFECTS.remove(name);
        if (effect != null) {
            effect.close();
        }
    }

    public static void cleanup() {
        EFFECTS.values().forEach(ShaderEffect::close);
        EFFECTS.clear();
    }

    public static void setEffectEnabled(String name, boolean enabled) {
        ShaderEffect effect = EFFECTS.get(name);
        if (effect != null) {
            effect.setEnabled(enabled);
        }
    }

    public static boolean isEffectEnabled(String name) {
        ShaderEffect effect = EFFECTS.get(name);
        return effect != null && effect.isEnabled();
    }

    public static Set<String> getLoadedEffectNames() {
        return Collections.unmodifiableSet(EFFECTS.keySet());
    }

    public static int getActiveEffectCount() {
        return (int) EFFECTS.values().stream()
                .filter(ShaderEffect::isEnabled)
                .count();
    }
}
