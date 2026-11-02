package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.api.content.MoonlightLampBlockEntityAccess;
import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** Asks the server to open the flashlight page for a placed lamp block. */
public final class ServerOpenLampBlockConfigPacket implements ServerPacket {
    private static final double MAX_DISTANCE_SQR = 64.0 * 64.0;

    private final BlockPos pos;

    public ServerOpenLampBlockConfigPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ServerOpenLampBlockConfigPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
    }

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
}
