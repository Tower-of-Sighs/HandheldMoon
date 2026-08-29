package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.neoforge.compat.clothconfig.HandheldMoonClothConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(HandheldMoon.MOD_ID)
public final class HandheldMoonNeoForge {
    public HandheldMoonNeoForge(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        HandheldMoon.init(modEventBus);
        if (dist == Dist.CLIENT) {
            modEventBus.addListener((FMLClientSetupEvent event) ->
                    event.enqueueWork(() -> HandheldMoonNeoForgeClientEvents.onClientSetup(event)));
        }
        if (dist == Dist.CLIENT) registerConfigMenu(modContainer);
    }

    private void registerConfigMenu(ModContainer modContainer) {
        if (LoadingModList.get().getModFileById("cloth_config") != null) {
            HandheldMoonClothConfigScreen.registerModsPage(modContainer);
        } else {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        }
    }
}
