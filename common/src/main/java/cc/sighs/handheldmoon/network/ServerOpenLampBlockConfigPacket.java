package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** Asks the server to open the flashlight page for a placed lamp block. */
public record ServerOpenLampBlockConfigPacket(BlockPos pos) implements CustomPacketPayload {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    public static final Type<ServerOpenLampBlockConfigPacket> TYPE =
            new Type<>(HandheldMoon.id("server_open_lamp_block_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerOpenLampBlockConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ServerOpenLampBlockConfigPacket::pos,
                    ServerOpenLampBlockConfigPacket::new);

    public void apply(ServerPlayer player) {
        if (player.distanceToSqr(pos.getCenter()) > MAX_DISTANCE_SQR) {
            return;
        }
        if (!(player.level().getBlockEntity(pos) instanceof MoonlightLampBlockEntityAccess)) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new FlashlightConfigMenu(containerId, inventory, pos),
                Component.translatable("screen.handheldmoon.flashlight_config.title")));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
