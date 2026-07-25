package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.FullMoonBlockEntity;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import net.minecraft.core.BlockPos;

@NetworkPacket(modId = HandheldMoon.MOD_ID, id = "server_full_moon_config_sync", side = Side.BOTH)
public record ServerFullMoonConfigSyncPacket(
        BlockPos blockPos,
        FullMoonDeviceConfig config
) implements INetworkPacket<ServerFullMoonConfigSyncPacket> {
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
            if (context.sender().level().getBlockEntity(blockPos) instanceof FullMoonBlockEntity moon) {
                moon.setFullMoonConfig(config);
            }
        });
    }
}
