package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.HandheldMoon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class EffectManager {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Logger LOGGER = LogManager.getLogger(HandheldMoon.MOD_ID);

    private static final Map<String, ShaderEffect> EFFECTS = new ConcurrentHashMap<>();
    private static int lastWindowWidth = -1;
    private static int lastWindowHeight = -1;

    private static class ShaderEffect {
        private final String jsonPath;
        private PostChain postChain;
        private boolean enabled = true;
        private boolean needsResize = true;

        public ShaderEffect(String jsonPath) {
            this.jsonPath = jsonPath;
            this.postChain = createPostChain(jsonPath);
        }

        private PostChain createPostChain(String path) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, path);
            try {
                return new PostChain(MC.getTextureManager(), MC.getResourceManager(),
                        MC.getMainRenderTarget(), location);
            } catch (IOException e) {
                return null;
            }
        }

        public boolean isValid() {
            return postChain != null && !postChain.passes.isEmpty();
        }

        public List<PostPass> getPasses() {
            return postChain != null ? postChain.passes : Collections.emptyList();
        }

        public void resize(int width, int height) {
            if (postChain != null && needsResize) {
                postChain.resize(width, height);
                needsResize = false;
            }
        }

        public void process(float partialTick) {
            if (postChain != null && enabled) {
                postChain.process(partialTick);
            }
        }

        public void close() {
            if (postChain != null) {
                postChain.close();
                postChain = null;
            }
        }

        public void markForResize() {
            this.needsResize = true;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            MC.execute(EffectManager::reloadAllShaders);
        });
    }


    private static void reloadAllShaders() {

        EFFECTS.values().forEach(ShaderEffect::close);

        // 重新创建所有着色器
        EFFECTS.replaceAll((name, oldEffect) -> {
            ShaderEffect newEffect = new ShaderEffect(oldEffect.jsonPath);
            if (!newEffect.isValid()) {
                return oldEffect; // 保持旧的着色器以防万一
            }
            return newEffect;
        });

        // 标记需要调整大小
        markAllForResize();
    }

    // 获取着色器效果
    public static List<PostPass> getEffect(String name) {
        ShaderEffect effect = EFFECTS.get(name);
        return effect != null ? effect.getPasses() : Collections.emptyList();
    }

    // 加载着色器效果
    public static boolean loadEffect(String name, String jsonPath) {
        if (EFFECTS.containsKey(name)) {
            return EFFECTS.get(name).isValid();
        }
        ShaderEffect effect = new ShaderEffect(jsonPath);
        if (effect.isValid()) {
            EFFECTS.put(name, effect);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isLoading(String name) {
        return EFFECTS.containsKey(name);
    }

    public static boolean isValid(String name) {
        ShaderEffect effect = EFFECTS.get(name);
        return effect != null && effect.isValid();
    }

    public static void initAll() {
        // 这个方法现在只是标记需要调整大小
        markAllForResize();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            checkAndHandleResize();
            EFFECTS.values().forEach(effect -> effect.process(event.getPartialTick().getGameTimeDeltaPartialTick(true)));
            MC.getMainRenderTarget().bindWrite(false);
        }
    }

    // 检查并处理窗口大小变化
    private static void checkAndHandleResize() {
        int currentWidth = MC.getWindow().getWidth();
        int currentHeight = MC.getWindow().getHeight();

        if (currentWidth != lastWindowWidth || currentHeight != lastWindowHeight) {
            lastWindowWidth = currentWidth;
            lastWindowHeight = currentHeight;
            markAllForResize();
        }

        // 调整所有需要调整大小的着色器
        EFFECTS.values().forEach(effect -> effect.resize(currentWidth, currentHeight));
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