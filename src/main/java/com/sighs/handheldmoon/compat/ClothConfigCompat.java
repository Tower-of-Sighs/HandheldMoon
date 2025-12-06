package com.sighs.handheldmoon.compat;

import com.sighs.handheldmoon.HandheldMoon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClothConfigCompat {
    private ClothConfigCompat() {
    }

    @SubscribeEvent
    public static void onEnqueue(final InterModEnqueueEvent event) {
        event.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT && ModList.get().isLoaded("cloth_config")) {
                ModLoadingContext.get().registerExtensionPoint(
                        ConfigScreenHandler.ConfigScreenFactory.class,
                        () -> new ConfigScreenHandler.ConfigScreenFactory(
                                (client, parent) -> HandheldMoonClothConfigScreen.getConfigBuilder()
                                        .setParentScreen(parent)
                                        .build()
                        )
                );
            }
        });
    }
}