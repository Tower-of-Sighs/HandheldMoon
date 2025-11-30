package com.sighs.handheldmoon.init;

import com.sighs.handheldmoon.api.PostChainAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class EffectManager {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final Map<String, PostChain> CHAINS = new LinkedHashMap<>();

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            mc.execute(EffectManager::initAll);
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

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            CHAINS.values().forEach(chain -> {
                chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                chain.process(event.getPartialTick());
            });
            mc.getMainRenderTarget().bindWrite(false);
        }
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
        ResourceLocation rl = new ResourceLocation(MODID, name);
        try {
            return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), rl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}