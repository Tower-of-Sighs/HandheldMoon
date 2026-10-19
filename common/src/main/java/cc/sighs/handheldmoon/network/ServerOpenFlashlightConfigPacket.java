package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** Asks the server to open the flashlight configuration menu for the held lamp. */
public record ServerOpenFlashlightConfigPacket() implements CustomPacketPayload {
    public static final Type<ServerOpenFlashlightConfigPacket> TYPE =
            new Type<>(HandheldMoon.id("server_open_flashlight_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerOpenFlashlightConfigPacket> STREAM_CODEC =
            StreamCodec.unit(new ServerOpenFlashlightConfigPacket());

    public void apply(ServerPlayer player) {
        if (FlashlightConfigMenu.resolveHand(player) == null) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new FlashlightConfigMenu(containerId, inventory),
                Component.translatable("screen.handheldmoon.flashlight_config.title")));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
