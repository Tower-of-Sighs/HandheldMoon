package cc.sighs.handheldmoon.network;

import cc.sighs.handheldmoon.menu.FlashlightConfigMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** Asks the server to open the flashlight configuration menu for the held lamp. */
public final class ServerOpenFlashlightConfigPacket implements ServerPacket {
    public ServerOpenFlashlightConfigPacket() {
    }

    public ServerOpenFlashlightConfigPacket(FriendlyByteBuf buf) {
    }

    public void encode(FriendlyByteBuf buf) {
    }

    public void apply(ServerPlayer player) {
        if (FlashlightConfigMenu.resolveHand(player) == null) {
            return;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, ignored) -> new FlashlightConfigMenu(containerId, inventory),
                Component.translatable("screen.handheldmoon.flashlight_config.title")));
    }
}
