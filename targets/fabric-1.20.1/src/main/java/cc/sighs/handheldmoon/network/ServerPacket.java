package cc.sighs.handheldmoon.network;

import net.minecraft.network.FriendlyByteBuf;

/** Common shape of every HandheldMoon server-bound packet on 1.20.1. */
public interface ServerPacket {
    void encode(FriendlyByteBuf buf);
}
