package cc.sighs.handheldmoon.neoforge.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.neoforge.compat.tacz.TaczCompat;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record ServerToggleAttachmentLampPacket() implements CustomPacketPayload {
    public static final Type<ServerToggleAttachmentLampPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(HandheldMoon.MOD_ID, "server_toggle_attachment_lamp"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerToggleAttachmentLampPacket> STREAM_CODEC = StreamCodec.unit(new ServerToggleAttachmentLampPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerToggleAttachmentLampPacket msg, IPayloadContext context) {
        context.enqueueWork(() -> TaczCompat.toggleAttachmentFlashlight(context.player()));
    }
}
