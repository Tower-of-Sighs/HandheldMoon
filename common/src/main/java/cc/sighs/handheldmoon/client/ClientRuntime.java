package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.dynamiclight.DynamicLightManager;
import cc.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import net.minecraft.client.Minecraft;

/** Shared client tick pipeline. Loader targets only provide event adapters. */
public final class ClientRuntime {
    private ClientRuntime() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (DynamicLightManager.syncLevel(minecraft)) {
            HandheldMoonDynamicLightsInitializer.reset();
        }
        HandheldMoonDynamicLightsInitializer.updateEntityBehaviors();
        DynamicLightManager.tick(minecraft);
    }
}
