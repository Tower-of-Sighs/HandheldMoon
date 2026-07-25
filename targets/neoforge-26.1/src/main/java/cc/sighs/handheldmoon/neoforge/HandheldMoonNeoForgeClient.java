package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.HandheldMoonClient;
import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = HandheldMoon.MOD_ID, dist = Dist.CLIENT)
public class HandheldMoonNeoForgeClient {
    public HandheldMoonNeoForgeClient(IEventBus eventBus) {
        HandheldMoonClient.initClient();
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (minecraft, parent) -> new ConfigScreen(parent, HandheldMoon.MOD_ID));
    }
}
