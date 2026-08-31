package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.HandheldMoonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = HandheldMoon.MOD_ID, dist = Dist.CLIENT)
public class HandheldMoonNeoForgeClient {
    public HandheldMoonNeoForgeClient(IEventBus eventBus, net.neoforged.fml.ModContainer modContainer) {
        eventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(HandheldMoonClient::initClient));
    }
}
