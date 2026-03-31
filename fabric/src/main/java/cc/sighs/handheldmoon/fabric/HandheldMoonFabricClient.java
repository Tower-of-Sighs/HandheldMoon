package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.client.HandheldMoonClient;
import net.fabricmc.api.ClientModInitializer;

public class HandheldMoonFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandheldMoonClient.initClient();
        HandheldMoonFabricClientEvents.register();
    }
}
