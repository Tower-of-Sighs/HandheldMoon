package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(HandheldMoon.MOD_ID)
public class HandheldMoonNeoForge {

    public HandheldMoonNeoForge(IEventBus eventBus, ModContainer modContainer) {
        HandheldMoon.init(eventBus);
    }
}
