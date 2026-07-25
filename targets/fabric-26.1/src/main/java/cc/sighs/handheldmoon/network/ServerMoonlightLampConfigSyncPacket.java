package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import net.minecraft.core.BlockPos;

@NetworkPacket(modId = HandheldMoon.MOD_ID, id = "server_moonlight_lamp_config_sync", side = Side.BOTH)
public record ServerMoonlightLampConfigSyncPacket(
        BlockPos blockPos,
        LampDeviceConfig config
) implements INetworkPacket<ServerMoonlightLampConfigSyncPacket> {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> {
            if (context.sender() == null) {
                return;
            }
            if (context.sender().distanceToSqr(blockPos.getCenter()) > MAX_DISTANCE_SQR) {
                return;
            }
            if (context.sender().level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntity lamp) {
                lamp.setLampConfig(config);
            }
        });
    }
}
