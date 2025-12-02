package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot, boolean powered) {
    public static void encode(ServerMoonLightLampSyncPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.blockPos);
        buffer.writeFloat(msg.xRot);
        buffer.writeFloat(msg.yRot);
        buffer.writeBoolean(msg.powered);
    }

    public static ServerMoonLightLampSyncPacket decode(FriendlyByteBuf buffer) {
        return new ServerMoonLightLampSyncPacket(buffer.readBlockPos(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(ServerMoonLightLampSyncPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender().serverLevel().getBlockEntity(msg.blockPos) instanceof MoonlightLampBlockEntity lamp) {
                lamp.setXRot(msg.xRot());
                lamp.setYRot(msg.yRot());
                lamp.setPowered(msg.powered);
                System.out.print("sync\n");
                lamp.setChanged();
            }
        });
    }
}
