package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class ServerMoonLightLampSyncPacket implements ServerPacket {
    private final BlockPos blockPos;
    private final float xRot;
    private final float yRot;
    private final boolean powered;

    public ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot, boolean powered) {
        this.blockPos = blockPos;
        this.xRot = xRot;
        this.yRot = yRot;
        this.powered = powered;
    }

    public ServerMoonLightLampSyncPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.xRot = buf.readFloat();
        this.yRot = buf.readFloat();
        this.powered = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeFloat(xRot);
        buf.writeFloat(yRot);
        buf.writeBoolean(powered);
    }

    public void apply(ServerPlayer player) {
        if (player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess lamp) {
            lamp.setXRot(xRot);
            lamp.setYRot(yRot);
            lamp.setPowered(powered);
        }
    }
}
