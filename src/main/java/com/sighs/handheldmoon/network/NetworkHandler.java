package com.sighs.handheldmoon.network;


import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkHandler {
    public static void init() {
        PayloadTypeRegistry.playC2S().register(
                ServerMoonLightLampSyncPacket.TYPE,
                ServerMoonLightLampSyncPacket.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                ServerMoonLightLampSyncPacket.TYPE,
                ServerMoonLightLampSyncPacket.STREAM_CODEC
        );
        ServerPlayNetworking.registerGlobalReceiver(ServerMoonLightLampSyncPacket.TYPE, ServerMoonLightLampSyncPacket::handle);
    }
}
