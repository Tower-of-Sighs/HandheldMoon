package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public record ServerFullMoonConfigSyncPacket(BlockPos blockPos, FullMoonDeviceConfig config)
        implements CustomPacketPayload {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    public static final Type<ServerFullMoonConfigSyncPacket> TYPE =
            new Type<>(HandheldMoon.id("server_full_moon_config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerFullMoonConfigSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerFullMoonConfigSyncPacket::blockPos,
                    ByteBufCodecs.fromCodec(DeviceConfigCodecs.FULL_MOON), ServerFullMoonConfigSyncPacket::config,
                    ServerFullMoonConfigSyncPacket::new);

    public void apply(ServerPlayer player) {
        if (player.distanceToSqr(blockPos.getCenter()) <= MAX_DISTANCE_SQR
                && player.level().getBlockEntity(blockPos) instanceof FullMoonBlockEntityAccess moon) {
            moon.setFullMoonConfig(config, true);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
