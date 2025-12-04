package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.api.PostChainAccessor; // 假设你移植了 Accessor 接口
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class EffectManager {
    private static final Minecraft mc = Minecraft.getInstance();
    public static final Map<String, PostChain> CHAINS = new LinkedHashMap<>();

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            // 确保在主线程执行初始化，防止 GL 错误
            mc.execute(EffectManager::initAll);
        });
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

            CHAINS.values().forEach(chain -> {
                chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                chain.process(partialTick);
            });

            // 绑定回主渲染目标，防止后续渲染出错
            mc.getMainRenderTarget().bindWrite(false);
        }
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
        ResourceLocation rl = name.indexOf(':') >= 0 ? ResourceLocation.parse(name) : ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, name);
        try {
            return new PostChain(mc.getTextureManager(), mc.getResourceManager(), mc.getMainRenderTarget(), rl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}