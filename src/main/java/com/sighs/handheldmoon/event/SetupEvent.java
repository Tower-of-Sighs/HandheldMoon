package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.registry.BlockEntities;
import com.sighs.handheldmoon.registry.Entities;
import com.sighs.handheldmoon.registry.KeyBindings;
import com.sighs.handheldmoon.registry.Items;
import com.sighs.handheldmoon.render.FullMoonRenderer;
import com.sighs.handheldmoon.render.MoonlightLampRenderer;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.sighs.handheldmoon.HandheldMoon.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SetupEvent {
    @SubscribeEvent
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    Items.MOONLIGHT_LAMP.get(),
                    new ResourceLocation(MODID, "powered"),
                    (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            DynamicLightHandlers.registerDynamicLightHandler(Entities.MOONLIGHT.get(), entity -> 15);
        });
        event.enqueueWork(CuriosCompat::init);
    }

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.FLASHLIGHT_SWITCH);
    }

    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        event.registerEntityRenderer(Entities.MOONLIGHT.get(), FullMoonRenderer::new);
    }

    @SubscribeEvent
    public static void onModelRegistry(ModelEvent.RegisterAdditional event) {
        ResourceLocation modelLocation = new ResourceLocation("handheldmoon:moonlight_lamp_on");
        ModelResourceLocation result = new ModelResourceLocation(modelLocation, "inventory");
        event.register(result);
    }
}
