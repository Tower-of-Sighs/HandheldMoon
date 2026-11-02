package cc.sighs.handheldmoon.fabric.network;

import cc.sighs.handheldmoon.fabric.compat.tacz.TaczCompat;
import cc.sighs.handheldmoon.network.ServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/** Server-side half of the TACZ attachment toggle: re-applies the new state. */
public final class ServerToggleAttachmentLampPacket implements ServerPacket {
    public ServerToggleAttachmentLampPacket() {
    }

    public ServerToggleAttachmentLampPacket(FriendlyByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public void apply(ServerPlayer player) {
        TaczCompat.toggleAttachmentFlashlight(player);
    }
}
