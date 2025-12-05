package com.sighs.handheldmoon.network;


import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
public class NetworkHandler {
    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(ServerMoonLightLampSyncPacket.TYPE, ServerMoonLightLampSyncPacket::handle);
        ServerPlayNetworking.registerGlobalReceiver(ServerToggleAttachmentLampPacket.TYPE, ServerToggleAttachmentLampPacket::handle);
    }
}
