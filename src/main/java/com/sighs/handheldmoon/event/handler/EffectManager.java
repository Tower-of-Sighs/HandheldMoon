package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.api.PostChainAccessor;
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EffectManager {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final Map<String, PostChain> CHAINS = new LinkedHashMap<>();

    public static void init() {
        WorldRenderEvents.LAST.register(EffectManager::onRenderLevelStage);
        onRegisterClientReloadListeners();
    }

    public static void onRegisterClientReloadListeners() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {

                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(HandheldMoon.MOD_ID, "effect_manager_reload");
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        mc.execute(EffectManager::initAll);
                    }
                });
    }

    public static List<PostPass> getEffect(String name) {
        PostChainAccessor postChain = (PostChainAccessor) CHAINS.get(name);
        return postChain.getPasses();
    }

    public static void loadEffect(String name, String jsonPath) {
        if (!CHAINS.containsKey(name)) CHAINS.put(name, createPostChain(jsonPath));
    }

    public static boolean isLoading(String name) {
        return CHAINS.containsKey(name);
    }

    public static void initAll() {
        CHAINS.replaceAll((name, chain) -> createPostChain(chain.getName()));
    }

    public static void onRenderLevelStage(WorldRenderContext context) {
        CHAINS.values().forEach(chain -> {
            chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            chain.process(context.tickDelta());
        });
        mc.getMainRenderTarget().bindWrite(false);
    }


    public static void clean(String name) {
        if (CHAINS.containsKey(name)) {
            CHAINS.get(name).close();
            CHAINS.remove(name);
        }
    }

    public static void cleanup() {
        CHAINS.values().forEach(PostChain::close);
        CHAINS.clear();
    }

    private static PostChain createPostChain(String name) {
        ResourceLocation rl = name.indexOf(':') >= 0 ? new ResourceLocation(name) : new ResourceLocation(HandheldMoon.MOD_ID, name);
        try {
            return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), rl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}