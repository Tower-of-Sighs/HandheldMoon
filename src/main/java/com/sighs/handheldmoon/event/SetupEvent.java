package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.registry.BlockEntities;
import com.sighs.handheldmoon.registry.KeyBindings;
import com.sighs.handheldmoon.registry.Items;
import com.sighs.handheldmoon.render.MoonlightLampRenderer;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
            DynamicLightHandlers.registerDynamicLightHandler(BlockEntities.MOONLIGHT_LAMP.get(), entity -> {
                return 15;
            });
            DynamicLightHandlers.registerDynamicLightHandler(EntityType.PIG, entity -> {
                return 15;
            });
            DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER, entity -> {
                return 15;
            });
            DynamicLightHandlers.registerDynamicLightHandler(BlockEntityType.BARREL,entity -> {
                return 15;
            });
            DynamicLightHandlers.registerDynamicLightHandler(BlockEntities.MOONLIGHT_LAMP.get(),entity -> {
                return 15;
            });
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
    }
}
