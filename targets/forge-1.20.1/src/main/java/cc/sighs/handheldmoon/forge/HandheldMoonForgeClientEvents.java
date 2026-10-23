package cc.sighs.handheldmoon.forge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.forge.client.HandheldMoonForgeClient;
import cc.sighs.handheldmoon.event.handler.EffectManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Mod-bus client registration for Forge 1.20.1. */
@Mod.EventBusSubscriber(modid = HandheldMoon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class HandheldMoonForgeClientEvents {
    private HandheldMoonForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        HandheldMoonForgeClient.onClientSetup(event);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        HandheldMoonForgeClient.onRegisterKeyMappings(event);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        HandheldMoonForgeClient.onRegisterItemColors(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        HandheldMoonForgeClient.onRegisterRenderers(event);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        HandheldMoonForgeClient.onRegisterModels(event);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        EffectManager.onRegisterClientReloadListeners(event);
    }
}
