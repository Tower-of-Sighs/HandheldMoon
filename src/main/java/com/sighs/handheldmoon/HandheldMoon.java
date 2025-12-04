package com.sighs.handheldmoon;

import com.mojang.logging.LogUtils;
import com.sighs.handheldmoon.network.NetworkHandler;
import com.sighs.handheldmoon.registry.*;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

public class HandheldMoon implements ModInitializer {

    public static final String MOD_ID = "handheldmoon";

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.SPEC);
        NetworkHandler.init();
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTab.init();
        ModEntities.init();
        ModBlockEntities.init();
    }
}
