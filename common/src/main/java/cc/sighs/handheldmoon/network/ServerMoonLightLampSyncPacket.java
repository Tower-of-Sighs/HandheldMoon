package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot, boolean powered)
        implements CustomPacketPayload {
    public static final Type<ServerMoonLightLampSyncPacket> TYPE =
            new Type<>(HandheldMoon.id("server_moonlight_lamp_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerMoonLightLampSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerMoonLightLampSyncPacket::blockPos,
                    ByteBufCodecs.FLOAT, ServerMoonLightLampSyncPacket::xRot,
                    ByteBufCodecs.FLOAT, ServerMoonLightLampSyncPacket::yRot,
                    ByteBufCodecs.BOOL, ServerMoonLightLampSyncPacket::powered,
                    ServerMoonLightLampSyncPacket::new);

    public void apply(ServerPlayer player) {
        if (player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess lamp) {
            lamp.setXRot(xRot);
            lamp.setYRot(yRot);
            lamp.setPowered(powered);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
