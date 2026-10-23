package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class ServerMoonlightLampConfigSyncPacket implements ServerPacket {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    private final BlockPos blockPos;
    private final LampDeviceConfig config;

    public ServerMoonlightLampConfigSyncPacket(BlockPos blockPos, LampDeviceConfig config) {
        this.blockPos = blockPos;
        this.config = config;
    }

    public ServerMoonlightLampConfigSyncPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        Tag tag = buf.readNbt();
        this.config = tag == null ? LampDeviceConfig.fromGlobalConfig()
                : DeviceConfigCodecs.LAMP.parse(NbtOps.INSTANCE, tag)
                .result().orElseGet(LampDeviceConfig::fromGlobalConfig);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeNbt((net.minecraft.nbt.CompoundTag) DeviceConfigCodecs.LAMP
                .encodeStart(NbtOps.INSTANCE, config).result().orElseThrow());
    }

    public void apply(ServerPlayer player) {
        if (player.distanceToSqr(blockPos.getCenter()) <= MAX_DISTANCE_SQR
                && player.level().getBlockEntity(blockPos) instanceof MoonlightLampBlockEntityAccess lamp) {
            // The lens slot is server-authoritative; keep its color when a
            // possibly stale client copy arrives with the numeric settings.
            LampDeviceConfig current = lamp.getLampConfig();
            lamp.setLampConfig(config.withLensColor(current.lensColor()), true);
        }
    }
}
