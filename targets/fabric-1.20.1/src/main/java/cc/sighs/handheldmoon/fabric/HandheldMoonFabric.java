package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.fabric.compat.tacz.TaczCompat;
import cc.sighs.handheldmoon.fabric.network.FabricNetwork;
import cc.sighs.handheldmoon.registry.ModRegistries;
import net.fabricmc.api.ModInitializer;

/** Fabric 1.20.1 entry point: shared bootstrap, registries and server-bound packets. */
public final class HandheldMoonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HandheldMoon.init(null);
        ModRegistries.initialize();
        TaczCompat.init();
        FabricNetwork.registerServerReceivers();
    }
}
