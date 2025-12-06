package com.sighs.handheldmoon.compat.modmenu;

import com.sighs.handheldmoon.compat.clothconfig.HandheldMoonClothConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.loader.api.FabricLoader;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (FabricLoader.getInstance().isModLoaded("cloth-config2")) {
            return screen -> HandheldMoonClothConfigScreen.getConfigBuilder().setParentScreen(screen).build();
        }
        return screen -> null;
    }
}
