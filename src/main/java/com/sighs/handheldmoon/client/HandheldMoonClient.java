package com.sighs.handheldmoon.client;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import com.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import com.sighs.handheldmoon.compat.TrinketsCompat;
import com.sighs.handheldmoon.event.handler.*;
import com.sighs.handheldmoon.item.MoonlightLampItem;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.registry.ModItems;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;


public class HandheldMoonClient implements ClientModInitializer, ModelLoadingPlugin {

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(this);
        ModKeyBindings.register();
        TrinketsCompat.init();
        ItemProperties.register(
                ModItems.MOONLIGHT_LAMP,
                new ResourceLocation(HandheldMoon.MOD_ID, "powered"),
                (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
        );


        registerBlockEntityRenders();
        registerEntityRenders();

        EffectManager.init();
        InteractEventHandler.init();
        OperationEventHandler.init();
        RayEvent.init();
        ShaderEventHandler.init();
        ClientTickEvents.START_WORLD_TICK.register(level -> HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors());
    }

    private static void registerBlockEntityRenders() {
        BlockEntityRenderers.register(ModBlockEntities.MOONLIGHT_LAMP, MoonlightLampRenderer::new);
    }

    private static void registerEntityRenders() {
        EntityRendererRegistry.register(ModEntities.MOONLIGHT, FullMoonRenderer::new);
    }

    @Override
    public void onInitializeModelLoader(Context context) {
        context.addModels(new ModelResourceLocation(new ResourceLocation(HandheldMoon.MOD_ID, "moonlight_lamp_on"), "inventory"));
    }
}
