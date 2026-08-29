package cc.sighs.handheldmoon.client;

import cc.sighs.handheldmoon.spi.PlatformServices;

public final class HandheldMoonClient {
    private HandheldMoonClient() {
    }

    public static void initClient() {
        PlatformServices.require().client().initializeClient();
    }

    public static void onClientTick() {
        PlatformServices.require().client().tickClient();
    }
}
