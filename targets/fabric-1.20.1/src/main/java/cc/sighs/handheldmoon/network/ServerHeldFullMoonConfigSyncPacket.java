package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.item.ItemData;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public final class ServerHeldFullMoonConfigSyncPacket implements ServerPacket {
    private final int handId;
    private final FullMoonDeviceConfig config;

    public ServerHeldFullMoonConfigSyncPacket(int handId, FullMoonDeviceConfig config) {
        this.handId = handId;
        this.config = config;
    }

    public ServerHeldFullMoonConfigSyncPacket(FriendlyByteBuf buf) {
        this.handId = buf.readVarInt();
        Tag tag = buf.readNbt();
        this.config = tag == null ? FullMoonDeviceConfig.fromGlobalConfig()
                : DeviceConfigCodecs.FULL_MOON.parse(NbtOps.INSTANCE, tag)
                .result().orElseGet(FullMoonDeviceConfig::fromGlobalConfig);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(handId);
        buf.writeNbt((CompoundTag) DeviceConfigCodecs.FULL_MOON
                .encodeStart(NbtOps.INSTANCE, config).result().orElseThrow());
    }

    public void apply(ServerPlayer player) {
        InteractionHand hand = handId == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(hand);
        if (stack.is(ModItems.FULL_MOON.get())) {
            ItemData.setFullMoonConfig(stack, config);
            player.setItemInHand(hand, stack);
        }
    }
}
