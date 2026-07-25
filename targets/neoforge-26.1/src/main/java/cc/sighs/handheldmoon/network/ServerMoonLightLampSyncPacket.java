package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import net.minecraft.core.BlockPos;

@NetworkPacket(modId = HandheldMoon.MOD_ID, id = "server_moonlight_lamp_sync", side = Side.BOTH)
public record ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot,
                                            boolean powered) implements INetworkPacket<ServerMoonLightLampSyncPacket> {
    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> {
            if (context.sender() != null && context.sender().level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntity lamp) {
                lamp.setXRot(xRot);
                lamp.setYRot(yRot);
                lamp.setPowered(powered);
            }
        });
    }
}