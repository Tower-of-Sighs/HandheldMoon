package com.sighs.handheldmoon.util;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.entity.FullMoonEntity;
import com.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ClientUtils {

    public static MoonlightLampBlockEntity getPoweredMoonlightLampBlock(FullMoonEntity entity) {
        if (entity.level().getBlockEntity(entity.blockPosition()) instanceof MoonlightLampBlockEntity lamp) {
            if (lamp.getPowered()) {
                return lamp;
            }
        }
        return null;
    }

    public static MoonlightLampBlockEntity getCursorMoonlightLampBlock() {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp) {
                return lamp;
            }
        }
        return null;
    }

    public static void syncMoonlightLampBlock(MoonlightLampBlockEntity lamp) {
        ClientPlayNetworking.send(ServerMoonLightLampSyncPacket.TYPE,
                encodePacket(new ServerMoonLightLampSyncPacket(
                        lamp.getBlockPos(),
                        lamp.getXRot(),
                        lamp.getYRot(),
                        lamp.getPowered()
                ))
        );
    }

    private static FriendlyByteBuf encodePacket(ServerMoonLightLampSyncPacket packet) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        ServerMoonLightLampSyncPacket.encode(packet, buf);
        return buf;
    }
}