package com.sighs.handheldmoon.client;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import com.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.registry.ModItems;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;


@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class SetupEvent {
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    ModItems.MOONLIGHT_LAMP.get(),
                    HandheldMoon.id("powered"),
                    (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            CuriosCompat.init();
        });
    }

    @SubscribeEvent
    public static void startWordTick(ClientTickEvent.Pre event) {
        HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors();
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.FLASHLIGHT_SWITCH);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        event.registerEntityRenderer(ModEntities.MOONLIGHT.get(), FullMoonRenderer::new);
    }

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on")));
    }
}
