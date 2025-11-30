package com.sighs.handheldmoon;

import com.sighs.handheldmoon.registry.Config;
import com.sighs.handheldmoon.registry.ModItems;
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
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                Config.SPEC
        );
    }
}
