package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.compat.tacz.TaczCompat;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;


public record ServerToggleAttachmentLampPacket() implements FabricPacket {
    public static final PacketType<ServerToggleAttachmentLampPacket> TYPE = PacketType.create(new ResourceLocation(HandheldMoon.MOD_ID, "server_toggle_attachment_lamp"), ServerToggleAttachmentLampPacket::new);

    public ServerToggleAttachmentLampPacket(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void handle(ServerToggleAttachmentLampPacket msg, ServerPlayer player, PacketSender responseSender) {
            TaczCompat.toggleAttachmentFlashlight(player);
    }
}