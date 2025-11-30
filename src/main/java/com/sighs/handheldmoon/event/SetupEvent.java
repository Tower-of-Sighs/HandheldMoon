package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.Item.MoonlightLampItem;
import com.sighs.handheldmoon.compat.CuriosCompat;
import com.sighs.handheldmoon.registry.KeyBindings;
import com.sighs.handheldmoon.registry.ModItems;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSource;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSources;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
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
                    ModItems.MOONLIGHT_LAMP.get(),
                    new ResourceLocation(MODID, "powered"),
                    (stack, world, entity, seed) -> MoonlightLampItem.getPowered(stack)
            );
            DynamicLightHandlers.registerDynamicLightHandler(EntityType.PLAYER, entity -> {
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
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(CuriosCompat::init);
    }
}
