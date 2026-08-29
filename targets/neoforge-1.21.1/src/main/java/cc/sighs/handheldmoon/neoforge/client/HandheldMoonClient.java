package cc.sighs.handheldmoon.neoforge.client;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.raycone.RayConeRenderer;
import cc.sighs.handheldmoon.neoforge.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.neoforge.client.renderer.LegacyRayConeRenderer;
import cc.sighs.handheldmoon.neoforge.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.neoforge.compat.curios.CuriosCompat;
import cc.sighs.handheldmoon.event.handler.LampConeSourceHooks;
import cc.sighs.handheldmoon.neoforge.event.handler.LampConeSourceProvider;
import cc.sighs.handheldmoon.neoforge.item.MoonlightLampItem;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import cc.sighs.handheldmoon.registry.ModItems;
import cc.sighs.handheldmoon.client.ClientRuntime;
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


public class HandheldMoonClient {
    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RayConeRenderer.installBackend(LegacyRayConeRenderer::render);
            LampConeSourceHooks.install(LampConeSourceProvider::append);
            ItemProperties.register(
                    ModItems.MOONLIGHT_LAMP.get(),
                    HandheldMoon.id("powered"),
                    (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            CuriosCompat.init();
        });
    }

    public static void startWordTick(ClientTickEvent.Pre event) {
        ClientRuntime.tick();
    }

    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                (net.minecraft.world.level.block.entity.BlockEntityType<cc.sighs.handheldmoon.neoforge.block.MoonlightLampBlockEntity>)
                        ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        event.registerEntityRenderer(
                (net.minecraft.world.entity.EntityType<cc.sighs.handheldmoon.neoforge.entity.FullMoonEntity>) ModEntities.MOONLIGHT.get(),
                FullMoonRenderer::new);
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "item/moonlight_lamp_on")));
    }
}
