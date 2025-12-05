package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.compat.TaczCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerToggleAttachmentLampPacket() {
    public static void encode(ServerToggleAttachmentLampPacket msg, FriendlyByteBuf buffer) {}

    public static ServerToggleAttachmentLampPacket decode(FriendlyByteBuf buffer) {
        return new ServerToggleAttachmentLampPacket();
    }

    public static void handle(ServerToggleAttachmentLampPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            TaczCompat.toggleAttachmentFlashlight(ctx.get().getSender());
        });
    }
}
