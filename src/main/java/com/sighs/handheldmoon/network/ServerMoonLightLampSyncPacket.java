package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public class ServerMoonLightLampSyncPacket {
    public static final ResourceLocation TYPE = new ResourceLocation(HandheldMoon.MOD_ID, "server_moon_light_lamp_sync");

    private final BlockPos blockPos;
    private final float xRot;
    private final float yRot;
    private final boolean powered;

    public ServerMoonLightLampSyncPacket(BlockPos blockPos, float xRot, float yRot, boolean powered) {
        this.blockPos = blockPos;
        this.xRot = xRot;
        this.yRot = yRot;
        this.powered = powered;
    }

    public static void encode(ServerMoonLightLampSyncPacket msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.blockPos);
        buffer.writeFloat(msg.xRot);
        buffer.writeFloat(msg.yRot);
        buffer.writeBoolean(msg.powered);
    }

    public static ServerMoonLightLampSyncPacket decode(FriendlyByteBuf buffer) {
        return new ServerMoonLightLampSyncPacket(
                buffer.readBlockPos(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean()
        );
    }

    public static void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        ServerMoonLightLampSyncPacket msg = decode(buf);

        server.execute(() -> {
            if (player.level().getBlockEntity(msg.blockPos) instanceof MoonlightLampBlockEntity lamp) {
                lamp.setXRot(msg.xRot);
                lamp.setYRot(msg.yRot);
                lamp.setPowered(msg.powered);
                lamp.setChanged();
            }
        });
    }
}