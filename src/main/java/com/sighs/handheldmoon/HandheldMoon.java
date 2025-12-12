package com.sighs.handheldmoon;

import com.mojang.logging.LogUtils;
import com.sighs.handheldmoon.compat.tacz.TaczCompat;
import com.sighs.handheldmoon.network.NetworkHandler;
import com.sighs.handheldmoon.registry.*;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

public class HandheldMoon implements ModInitializer {

    public static final String MOD_ID = "handheldmoon";

    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        TaczCompat.init();
        ConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.CLIENT, Config.SPEC);
        NetworkHandler.init();
        ModDataComponent.init();
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTab.init();
        ModEntities.init();
        ModBlockEntities.init();
    }
}
