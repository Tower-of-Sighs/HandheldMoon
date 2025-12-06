package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.compat.tacz.TaczCompat;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;


public record ServerToggleAttachmentLampPacket() implements CustomPacketPayload {
    public static final Type<ServerToggleAttachmentLampPacket> TYPE = new Type(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "server_toggle_attachment_lamp"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerToggleAttachmentLampPacket> STREAM_CODEC = StreamCodec.unit(new ServerToggleAttachmentLampPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerToggleAttachmentLampPacket msg, ServerPlayNetworking.Context context) {
        TaczCompat.toggleAttachmentFlashlight(context.player());
    }
}