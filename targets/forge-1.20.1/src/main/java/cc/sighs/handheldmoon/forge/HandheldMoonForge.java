package cc.sighs.handheldmoon.forge;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(HandheldMoon.MOD_ID)
public final class HandheldMoonForge {
    public HandheldMoonForge() {
        HandheldMoon.init(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
