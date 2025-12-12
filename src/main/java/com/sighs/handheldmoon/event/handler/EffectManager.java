package com.sighs.handheldmoon.event.handler;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import com.mojang.blaze3d.systems.RenderPass;
import com.sighs.handheldmoon.HandheldMoon;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class EffectManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("HandheldMoon/EffectManager");
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Map<String, ManagedPostChain> CHAINS = new LinkedHashMap<>();

    private static int lastWindowWidth = -1;
    private static int lastWindowHeight = -1;

    public static void init() {
        WorldRenderEvents.LAST.register(EffectManager::onRenderLevelStage);
        onRegisterClientReloadListeners();
        LOGGER.info("EffectManager initialized and world render hook registered");
    }

    public static void onRegisterClientReloadListeners() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "effect_manager_reload");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        MC.execute(EffectManager::reloadAll);
                    }
                });
    }

    public static List<PostPass> getEffect(String name) {
        ManagedPostChain chain = CHAINS.get(name);
        return (chain != null && chain.postChain != null) ? chain.postChain.passes : Collections.emptyList();
    }

    public static void loadEffect(String name, String jsonPath) {
        if (!CHAINS.containsKey(name)) {
            CHAINS.put(name, new ManagedPostChain(jsonPath));
        }
    }

    public static boolean isLoading(String name) {
        return CHAINS.containsKey(name);
    }

    public static boolean isValid(String name) {
        ManagedPostChain chain = CHAINS.get(name);
        return chain != null && chain.postChain != null && !chain.postChain.passes.isEmpty();
    }

    public static void reloadAll() {
        CHAINS.values().forEach(ManagedPostChain::close);
        CHAINS.replaceAll((name, old) -> new ManagedPostChain(old.jsonPath));
        markAllForResize();
    }

    public static void onRenderLevelStage(WorldRenderContext context) {
        checkAndHandleResize();

        for (ManagedPostChain chain : CHAINS.values()) {
            if (chain.postChain != null) {
                chain.process();
            }
        }

        CHAINS.values().forEach(ManagedPostChain::endFrame);
    }

    private static void checkAndHandleResize() {
        int w = MC.getWindow().getWidth();
        int h = MC.getWindow().getHeight();
        if (w != lastWindowWidth || h != lastWindowHeight) {
            lastWindowWidth = w;
            lastWindowHeight = h;
            markAllForResize();
        }
    }

    private static void markAllForResize() {
        CHAINS.values().forEach(chain -> chain.needsResize = true);
    }

    public static void clean(String name) {
        ManagedPostChain chain = CHAINS.remove(name);
        if (chain != null) {
            chain.close();
        }
    }

    public static void cleanup() {
        CHAINS.values().forEach(ManagedPostChain::close);
        CHAINS.clear();
    }

    public static void setEffectUniforms(String name, Consumer<RenderPass> consumer) {
        ManagedPostChain chain = CHAINS.get(name);
        if (chain != null) {
            chain.uniformSetter = consumer;
        }
    }

    private static class ManagedPostChain {
        final String jsonPath;
        PostChain postChain;
        boolean needsResize = true;
        final CrossFrameResourcePool resourcePool = new CrossFrameResourcePool(3);
        Consumer<RenderPass> uniformSetter;

        ManagedPostChain(String jsonPath) {
            this.jsonPath = jsonPath;
            reload();
        }

        void reload() {
            close();
            try {
                ResourceLocation rl = jsonPath.contains(":")
                        ? ResourceLocation.parse(jsonPath)
                        : ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, jsonPath);
                this.postChain = MC.getShaderManager().getPostChain(rl, Set.of(PostChain.MAIN_TARGET_ID));
            } catch (Exception e) {
                HandheldMoon.LOGGER.error("Failed to load post chain: {}", jsonPath, e);
                this.postChain = null;
            }
        }

        void process() {
            if (postChain == null) return;

            if (needsResize) {
                resourcePool.clear();
                needsResize = false;
            }

            Consumer<RenderPass> consumer = uniformSetter;
            postChain.process(MC.getMainRenderTarget(), resourcePool, consumer);
        }

        void endFrame() {
            resourcePool.endFrame();
        }

        void close() {
            if (postChain != null) {
                postChain = null;
            }
            resourcePool.close();
            uniformSetter = null;
        }
    }
}