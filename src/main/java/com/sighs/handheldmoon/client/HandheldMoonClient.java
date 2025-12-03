package com.sighs.handheldmoon.client;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import com.sighs.handheldmoon.compat.TrinketsCompat;
import com.sighs.handheldmoon.event.handler.*;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.registry.ModItems;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;


public class HandheldMoonClient implements ClientModInitializer{

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(out -> {
            out.addModels(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp"));
            out.addModels(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on"));
        });
        ModKeyBindings.register();
        TrinketsCompat.init();
        ItemProperties.register(
                ModItems.MOONLIGHT_LAMP,
                ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "powered"),
                (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
        );


        registerRenders();

        EffectManager.init();
        InteractEventHandler.init();
        OperationEventHandler.init();
        RayEvent.init();
        ShaderEventHandler.init();
        ClientTickEvents.START_WORLD_TICK.register(level -> HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors());
    }

    private static void registerRenders() {
        BlockEntityRenderers.register(ModBlockEntities.MOONLIGHT_LAMP, MoonlightLampRenderer::new);
    }
}
