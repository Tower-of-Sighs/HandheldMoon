package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.HandheldMoonClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import cc.sighs.handheldmoon.neoforge.compat.clothconfig.HandheldMoonClothConfigScreen;

@Mod(value = HandheldMoon.MOD_ID, dist = Dist.CLIENT)
public class HandheldMoonNeoForgeClient {
    public HandheldMoonNeoForgeClient(IEventBus eventBus, net.neoforged.fml.ModContainer modContainer) {
        eventBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(HandheldMoonClient::initClient));
        if (LoadingModList.get().getModFileById("cloth_config") != null) {
            HandheldMoonClothConfigScreen.registerModsPage(modContainer);
        } else {
            ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class,
                    () -> net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
        }
    }
}
