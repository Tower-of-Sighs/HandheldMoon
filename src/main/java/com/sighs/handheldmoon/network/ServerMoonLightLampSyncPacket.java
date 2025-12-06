package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot,
                                            boolean powered) implements CustomPacketPayload {

    public static final Type<ServerMoonLightLampSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "server_moon_light_lamp_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerMoonLightLampSyncPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerMoonLightLampSyncPacket::blockPos,
            ByteBufCodecs.FLOAT, ServerMoonLightLampSyncPacket::xRot,
            ByteBufCodecs.FLOAT, ServerMoonLightLampSyncPacket::yRot,
            ByteBufCodecs.BOOL, ServerMoonLightLampSyncPacket::powered,
            ServerMoonLightLampSyncPacket::new
    );

    public static void handle(ServerMoonLightLampSyncPacket msg, ServerPlayNetworking.Context context) {
        if (context.player().level().getBlockEntity(msg.blockPos) instanceof MoonlightLampBlockEntity lamp) {
            lamp.setXRot(msg.xRot());
            lamp.setYRot(msg.yRot());
            lamp.setPowered(msg.powered);
            lamp.setChanged();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}