package com.sighs.handheldmoon;

import com.sighs.handheldmoon.network.NetworkHandler;
import com.sighs.handheldmoon.registry.KeyBindings;
import com.sighs.handheldmoon.registry.ModDataComponent;
import com.sighs.handheldmoon.registry.ModItems;
import net.fabricmc.api.ModInitializer;

public class HandheldMoon implements ModInitializer {

    public static final String MOD_ID = "handheldmoon";

    @Override
    public void onInitialize() {
        NetworkHandler.init();
        KeyBindings.register();
        ModDataComponent.init();
        ModItems.init();
    }
}
