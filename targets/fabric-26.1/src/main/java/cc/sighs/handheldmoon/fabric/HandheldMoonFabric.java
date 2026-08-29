package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.HandheldMoon;
import net.fabricmc.api.ModInitializer;

public class HandheldMoonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        HandheldMoon.init(null);
    }
}
