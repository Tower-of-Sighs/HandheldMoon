package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(HandheldMoon.MOD_ID)
public final class HandheldMoonNeoForge {
    public HandheldMoonNeoForge(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        HandheldMoon.init(modEventBus);
        if (dist == Dist.CLIENT) {
            modEventBus.addListener((FMLClientSetupEvent event) ->
                    event.enqueueWork(() -> HandheldMoonNeoForgeClientEvents.onClientSetup(event)));
        }
    }
}
