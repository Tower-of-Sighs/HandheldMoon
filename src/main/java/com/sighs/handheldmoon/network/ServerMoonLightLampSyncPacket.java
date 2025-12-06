package com.sighs.handheldmoon.network;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ServerMoonLightLampSyncPacket(BlockPos pos, float xRot, float yRot,
                                            boolean powered) implements FabricPacket {
    public static final PacketType<ServerMoonLightLampSyncPacket> TYPE = PacketType.create(new ResourceLocation(HandheldMoon.MOD_ID, "server_moon_light_lamp_sync"), ServerMoonLightLampSyncPacket::new);

    public ServerMoonLightLampSyncPacket(FriendlyByteBuf buf) {
        this(
                buf.readBlockPos(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readBoolean()
        );
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(xRot);
        buffer.writeFloat(yRot);
        buffer.writeBoolean(powered);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

    public static void handle(ServerMoonLightLampSyncPacket msg, ServerPlayer player, PacketSender responseSender) {
        if (player.level().getBlockEntity(msg.pos) instanceof MoonlightLampBlockEntity lamp) {
            lamp.setXRot(msg.xRot);
            lamp.setYRot(msg.yRot);
            lamp.setPowered(msg.powered);
            lamp.setChanged();
        }
    }
}