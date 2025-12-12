package com.sighs.handheldmoon.client;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import com.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import com.sighs.handheldmoon.client.renderer.item.properties.conditional.MoonlightLampPoweredProperty;
import com.sighs.handheldmoon.compat.trinkets.TrinketsCompat;
import com.sighs.handheldmoon.event.handler.*;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.registry.ModBlockEntities;
import com.sighs.handheldmoon.registry.ModEntities;
import com.sighs.handheldmoon.registry.ModKeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.resources.ResourceLocation;


public class HandheldMoonClient implements ClientModInitializer {

    public static final ExtraModelKey<BlockStateModel> MOONLIGHT_LAMP_MODEL_KEY =
            ExtraModelKey.create(() -> HandheldMoon.MOD_ID + ":item/moonlight_lamp");
    public static final ExtraModelKey<BlockStateModel> MOONLIGHT_LAMP_ON_MODEL_KEY =
            ExtraModelKey.create(() -> HandheldMoon.MOD_ID + ":item/moonlight_lamp_on");

    @Override
    public void onInitializeClient() {
        ModelLoadingPlugin.register(context -> {
            context.addModel(
                    MOONLIGHT_LAMP_MODEL_KEY,
                    SimpleUnbakedExtraModel.blockStateModel(
                            ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp")
                    )
            );
            context.addModel(
                    MOONLIGHT_LAMP_ON_MODEL_KEY,
                    SimpleUnbakedExtraModel.blockStateModel(
                            ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on")
                    )
            );
        });
        ModKeyBindings.register();
        TrinketsCompat.init();

        ConditionalItemModelProperties.ID_MAPPER.put(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "powered"), MoonlightLampPoweredProperty.MAP_CODEC);

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
}
