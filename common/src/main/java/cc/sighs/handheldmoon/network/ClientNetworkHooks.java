package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;

import cc.sighs.handheldmoon.spi.PlatformServices;

public final class ClientNetworkHooks {
    private ClientNetworkHooks() {
    }

    public static void syncLampState(MoonlightLampBlockEntityAccess lamp) {
        PlatformServices.require().client().sendLampState(lamp.getBlockPos(), lamp.getXRot(), lamp.getYRot(), lamp.getPowered());
    }

    public static void syncLampBlockConfig(BlockPos pos, LampDeviceConfig config) {
        PlatformServices.require().client().sendLampConfig(pos, config);
    }

    public static void syncFullMoonBlockConfig(BlockPos pos, FullMoonDeviceConfig config) {
        PlatformServices.require().client().sendFullMoonConfig(pos, config);
    }

    public static void syncHeldLampConfig(InteractionHand hand, LampDeviceConfig config) {
        PlatformServices.require().client().sendHeldLampConfig(hand, config);
    }

    public static void syncHeldFullMoonConfig(InteractionHand hand, FullMoonDeviceConfig config) {
        PlatformServices.require().client().sendHeldFullMoonConfig(hand, config);
    }
}
