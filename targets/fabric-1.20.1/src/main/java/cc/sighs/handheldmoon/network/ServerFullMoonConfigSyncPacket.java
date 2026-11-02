package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.content.FullMoonBlockEntityAccess;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class ServerFullMoonConfigSyncPacket implements ServerPacket {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    private final BlockPos blockPos;
    private final FullMoonDeviceConfig config;

    public ServerFullMoonConfigSyncPacket(BlockPos blockPos, FullMoonDeviceConfig config) {
        this.blockPos = blockPos;
        this.config = config;
    }

    public ServerFullMoonConfigSyncPacket(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        Tag tag = buf.readNbt();
        this.config = tag == null ? FullMoonDeviceConfig.fromGlobalConfig()
                : DeviceConfigCodecs.FULL_MOON.parse(NbtOps.INSTANCE, tag)
                .result().orElseGet(FullMoonDeviceConfig::fromGlobalConfig);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeNbt((CompoundTag) DeviceConfigCodecs.FULL_MOON
                .encodeStart(NbtOps.INSTANCE, config).result().orElseThrow());
    }

    public void apply(ServerPlayer player) {
        if (player.distanceToSqr(blockPos.getCenter()) <= MAX_DISTANCE_SQR
                && player.level().getBlockEntity(blockPos) instanceof FullMoonBlockEntityAccess moon) {
            moon.setFullMoonConfig(config, true);
        }
    }
}
