package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class NeoForgeInteractEventBridge {
    private NeoForgeInteractEventBridge() {
    }

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (InteractEventHandler.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient() && event.getHand() == InteractionHand.MAIN_HAND) {
            InteractEventHandler.onClientMainHandRightClickBlock();
        }
    }
}
