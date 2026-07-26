package cc.sighs.handheldmoon.network;


import cc.sighs.handheldmoon.HandheldMoon;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID)
public class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(HandheldMoon.MOD_ID);
        registrar.playBidirectional(ServerMoonLightLampSyncPacket.TYPE, ServerMoonLightLampSyncPacket.STREAM_CODEC, ServerMoonLightLampSyncPacket::handle);
        registrar.playToServer(ServerToggleAttachmentLampPacket.TYPE, ServerToggleAttachmentLampPacket.STREAM_CODEC, ServerToggleAttachmentLampPacket::handle);
        registrar.playToServer(ServerMoonlightLampConfigSyncPacket.TYPE, ServerMoonlightLampConfigSyncPacket.STREAM_CODEC, ServerMoonlightLampConfigSyncPacket::handle);
        registrar.playToServer(ServerFullMoonConfigSyncPacket.TYPE, ServerFullMoonConfigSyncPacket.STREAM_CODEC, ServerFullMoonConfigSyncPacket::handle);
        registrar.playToServer(ServerHeldMoonlightLampConfigSyncPacket.TYPE, ServerHeldMoonlightLampConfigSyncPacket.STREAM_CODEC, ServerHeldMoonlightLampConfigSyncPacket::handle);
        registrar.playToServer(ServerHeldFullMoonConfigSyncPacket.TYPE, ServerHeldFullMoonConfigSyncPacket.STREAM_CODEC, ServerHeldFullMoonConfigSyncPacket::handle);
    }
}
