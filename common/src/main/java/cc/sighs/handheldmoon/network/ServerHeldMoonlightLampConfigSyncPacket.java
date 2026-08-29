package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.config.DeviceConfigCodecs;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.registry.ModDataComponent;
import cc.sighs.handheldmoon.registry.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public record ServerHeldMoonlightLampConfigSyncPacket(int handId, LampDeviceConfig config)
        implements CustomPacketPayload {
    public static final Type<ServerHeldMoonlightLampConfigSyncPacket> TYPE =
            new Type<>(HandheldMoon.id("server_held_moonlight_lamp_config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerHeldMoonlightLampConfigSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, ServerHeldMoonlightLampConfigSyncPacket::handId,
                    ByteBufCodecs.fromCodec(DeviceConfigCodecs.LAMP), ServerHeldMoonlightLampConfigSyncPacket::config,
                    ServerHeldMoonlightLampConfigSyncPacket::new);

    public void apply(ServerPlayer player) {
        InteractionHand hand = handId == 1 ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(hand);
        if (stack.is(ModItems.MOONLIGHT_LAMP.get())) {
            stack.set(ModDataComponent.LAMP_CONFIG.get(), config);
            player.setItemInHand(hand, stack);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
