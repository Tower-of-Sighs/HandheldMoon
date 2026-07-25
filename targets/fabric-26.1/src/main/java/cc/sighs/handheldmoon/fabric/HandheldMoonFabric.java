package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.oelib.network.api.NetworkAutoRegistration;
import net.fabricmc.api.ModInitializer;

public class HandheldMoonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NetworkAutoRegistration.registerBasePackage("cc.sighs.handheldmoon.network");
        HandheldMoon.init();
    }
}
