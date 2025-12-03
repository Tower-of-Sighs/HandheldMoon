package com.sighs.handheldmoon;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.registry.*;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandler;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HandheldMoon.MODID)
public class HandheldMoon {
    public static final String MODID = "handheldmoon";

    public HandheldMoon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Entities.ENTITIES.register(modEventBus);
        Items.ITEMS.register(modEventBus);
        Blocks.BLOCKS.register(modEventBus);
        BlockEntities.BLOCK_ENTITIES.register(modEventBus);
        Items.CREATIVE_MODE_TABS.register(modEventBus);

        NetworkHandler.register();

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                Config.SPEC
        );
    }
}
