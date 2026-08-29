package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record ServerHeldFullMoonConfigSyncPacket(int handId, FullMoonDeviceConfig config)
        implements CustomPacketPayload {
    public static final Type<ServerHeldFullMoonConfigSyncPacket> TYPE =
            new Type<>(HandheldMoon.id("server_held_full_moon_config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerHeldFullMoonConfigSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerHeldFullMoonConfigSyncPacket::handId,
                    ByteBufCodecs.fromCodec(DeviceConfigCodecs.FULL_MOON), ServerHeldFullMoonConfigSyncPacket::config,
                    ServerHeldFullMoonConfigSyncPacket::new);

    public void apply(ServerPlayer player) {
        InteractionHand hand = handId == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(hand);
        if (stack.is(ModItems.FULL_MOON.get())) {
            stack.set(ModDataComponent.FULL_MOON_CONFIG.get(), config);
            player.setItemInHand(hand, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
