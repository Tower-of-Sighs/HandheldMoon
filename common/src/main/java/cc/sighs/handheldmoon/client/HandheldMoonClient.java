package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.compat.curios.CuriosCompat;
import cc.sighs.handheldmoon.event.handler.ShaderEventHandler;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import cc.sighs.handheldmoon.registry.ModKeyBindings;

public final class HandheldMoonClient {
    private HandheldMoonClient() {
    }

    public static void initClient() {
        ModKeyBindings.register();
        CuriosCompat.init();
    }

    public static void onClientTick() {
        HandheldMoonDynamicLightsInitializer.updatePlayerBehaviors();
        ShaderEventHandler.onClientTick();
    }
}
