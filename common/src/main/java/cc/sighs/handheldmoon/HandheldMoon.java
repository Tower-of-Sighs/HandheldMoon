package cc.sighs.handheldmoon;

import cc.sighs.handheldmoon.spi.PlatformServices;
import cc.sighs.handheldmoon.spi.RegistryService;
import cc.sighs.handheldmoon.registry.Config;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

public final class HandheldMoon {
    public static final String MOD_ID = "handheldmoon";
    public static final Logger LOGGER = LogUtils.getLogger();

    private HandheldMoon() {
    }

    public static void init(Object loaderContext) {
        PlatformServices.initialize(loaderContext);
        Config.register();
        Config.load();
    }

    public static RegistryService registry() {
        return PlatformServices.require().registry();
    }

    public static net.minecraft.world.level.block.entity.BlockEntity createBlockEntity(
            String id, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        return registry().createBlockEntity(id, pos, state);
    }

    @SuppressWarnings("unchecked")
    public static <T> T id(String path) {
        return (T) PlatformServices.require().id(path);
    }

    public static String formattedMod(String path) {
        return path.formatted(MOD_ID);
    }
}
