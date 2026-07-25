package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.oelib.network.api.NetworkAutoRegistration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(HandheldMoon.MOD_ID)
public class HandheldMoonNeoForge {

    public HandheldMoonNeoForge(IEventBus eventBus) {
        NetworkAutoRegistration.registerBasePackage("cc.sighs.handheldmoon.network");
        HandheldMoon.init();
    }
}
