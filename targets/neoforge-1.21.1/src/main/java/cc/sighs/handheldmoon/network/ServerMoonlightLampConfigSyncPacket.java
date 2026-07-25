package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerMoonlightLampConfigSyncPacket(
        BlockPos blockPos,
        LampDeviceConfig config
) implements CustomPacketPayload {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    public static final Type<ServerMoonlightLampConfigSyncPacket> TYPE =
            new Type<>(HandheldMoon.id("server_moonlight_lamp_config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerMoonlightLampConfigSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    ServerMoonlightLampConfigSyncPacket::blockPos,
                    ByteBufCodecs.fromCodec(DeviceConfigCodecs.LAMP),
                    ServerMoonlightLampConfigSyncPacket::config,
                    ServerMoonlightLampConfigSyncPacket::new
            );

    public static void handle(ServerMoonlightLampConfigSyncPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().distanceToSqr(message.blockPos().getCenter()) > MAX_DISTANCE_SQR) {
                return;
            }
            if (context.player().level().getBlockEntity(message.blockPos()) instanceof MoonlightLampBlockEntity lamp) {
                lamp.setLampConfig(message.config(), true);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
