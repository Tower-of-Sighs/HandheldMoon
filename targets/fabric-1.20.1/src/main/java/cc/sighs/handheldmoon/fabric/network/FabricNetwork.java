package cc.sighs.handheldmoon.fabric.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.network.ServerFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldFullMoonConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerHeldMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonLightLampSyncPacket;
import cc.sighs.handheldmoon.network.ServerMoonlightLampConfigSyncPacket;
import cc.sighs.handheldmoon.network.ServerOpenFlashlightConfigPacket;
import cc.sighs.handheldmoon.network.ServerOpenLampBlockConfigPacket;
import cc.sighs.handheldmoon.network.ServerPacket;
import cc.sighs.handheldmoon.network.ServerSetLightProfilePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fabric 1.20.1 transport for the shared server-bound packets.
 *
 * <p>Fabric 0.92 predates the payload API, so every packet class gets its own
 * classic channel named {@code handheldmoon:<packet_name>}. Encoding stays in
 * the shared packet classes; this class only moves the buffer and re-dispatches
 * the decoded packet on the server thread.</p>
 */
public final class FabricNetwork {
    public static final ResourceLocation MOON_LIGHT_LAMP_SYNC =
            channel("server_moon_light_lamp_sync");
    public static final ResourceLocation MOONLIGHT_LAMP_CONFIG_SYNC =
            channel("server_moonlight_lamp_config_sync");
    public static final ResourceLocation FULL_MOON_CONFIG_SYNC =
            channel("server_full_moon_config_sync");
    public static final ResourceLocation HELD_MOONLIGHT_LAMP_CONFIG_SYNC =
            channel("server_held_moonlight_lamp_config_sync");
    public static final ResourceLocation HELD_FULL_MOON_CONFIG_SYNC =
            channel("server_held_full_moon_config_sync");
    public static final ResourceLocation OPEN_FLASHLIGHT_CONFIG =
            channel("server_open_flashlight_config");
    public static final ResourceLocation OPEN_LAMP_BLOCK_CONFIG =
            channel("server_open_lamp_block_config");
    public static final ResourceLocation SET_LIGHT_PROFILE =
            channel("server_set_light_profile");
    public static final ResourceLocation TOGGLE_ATTACHMENT_LAMP =
            channel("server_toggle_attachment_lamp");

    private static boolean registered;

    private FabricNetwork() {
    }

    private static ResourceLocation channel(String path) {
        return new ResourceLocation(HandheldMoon.MOD_ID, path);
    }

    /** Sends a packet to the server. Only ever called from the client. */
    public static void send(ServerPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.encode(buf);
        ClientPlayNetworking.send(channelOf(packet), buf);
    }

    private static ResourceLocation channelOf(ServerPacket packet) {
        if (packet instanceof ServerMoonLightLampSyncPacket) {
            return MOON_LIGHT_LAMP_SYNC;
        }
        if (packet instanceof ServerMoonlightLampConfigSyncPacket) {
            return MOONLIGHT_LAMP_CONFIG_SYNC;
        }
        if (packet instanceof ServerFullMoonConfigSyncPacket) {
            return FULL_MOON_CONFIG_SYNC;
        }
        if (packet instanceof ServerHeldMoonlightLampConfigSyncPacket) {
            return HELD_MOONLIGHT_LAMP_CONFIG_SYNC;
        }
        if (packet instanceof ServerHeldFullMoonConfigSyncPacket) {
            return HELD_FULL_MOON_CONFIG_SYNC;
        }
        if (packet instanceof ServerOpenFlashlightConfigPacket) {
            return OPEN_FLASHLIGHT_CONFIG;
        }
        if (packet instanceof ServerOpenLampBlockConfigPacket) {
            return OPEN_LAMP_BLOCK_CONFIG;
        }
        if (packet instanceof ServerSetLightProfilePacket) {
            return SET_LIGHT_PROFILE;
        }
        if (packet instanceof ServerToggleAttachmentLampPacket) {
            return TOGGLE_ATTACHMENT_LAMP;
        }
        throw new IllegalArgumentException("No HandheldMoon channel for " + packet.getClass().getName());
    }

    /** Registers every server-side receiver. Safe to call more than once. */
    public static void registerServerReceivers() {
        if (registered) {
            return;
        }
        registered = true;
        register(MOON_LIGHT_LAMP_SYNC, ServerMoonLightLampSyncPacket::new, ServerMoonLightLampSyncPacket::apply);
        register(MOONLIGHT_LAMP_CONFIG_SYNC, ServerMoonlightLampConfigSyncPacket::new,
                ServerMoonlightLampConfigSyncPacket::apply);
        register(FULL_MOON_CONFIG_SYNC, ServerFullMoonConfigSyncPacket::new,
                ServerFullMoonConfigSyncPacket::apply);
        register(HELD_MOONLIGHT_LAMP_CONFIG_SYNC, ServerHeldMoonlightLampConfigSyncPacket::new,
                ServerHeldMoonlightLampConfigSyncPacket::apply);
        register(HELD_FULL_MOON_CONFIG_SYNC, ServerHeldFullMoonConfigSyncPacket::new,
                ServerHeldFullMoonConfigSyncPacket::apply);
        register(OPEN_FLASHLIGHT_CONFIG, ServerOpenFlashlightConfigPacket::new,
                ServerOpenFlashlightConfigPacket::apply);
        register(OPEN_LAMP_BLOCK_CONFIG, ServerOpenLampBlockConfigPacket::new,
                ServerOpenLampBlockConfigPacket::apply);
        register(SET_LIGHT_PROFILE, ServerSetLightProfilePacket::new,
                ServerSetLightProfilePacket::apply);
        register(TOGGLE_ATTACHMENT_LAMP, ServerToggleAttachmentLampPacket::new,
                ServerToggleAttachmentLampPacket::apply);
    }

    private static <T extends ServerPacket> void register(
            ResourceLocation channel, Decoder<T> decoder, Applier<T> applier) {
        ServerPlayNetworking.registerGlobalReceiver(channel, (server, player, handler, buf, responseSender) -> {
            // The buffer is only valid inside this callback, so copy the raw
            // bytes and decode them on the server thread.
            byte[] payload = new byte[buf.readableBytes()];
            buf.readBytes(payload);
            server.execute(() -> {
                FriendlyByteBuf copy = PacketByteBufs.create();
                copy.writeBytes(payload);
                applier.apply(decoder.decode(copy), player);
            });
        });
    }

    @FunctionalInterface
    private interface Decoder<T extends ServerPacket> {
        T decode(FriendlyByteBuf buf);
    }

    @FunctionalInterface
    private interface Applier<T extends ServerPacket> {
        void apply(T packet, ServerPlayer player);
    }
}
