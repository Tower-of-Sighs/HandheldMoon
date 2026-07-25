package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeInteractEventBridge {
    private NeoForgeInteractEventBridge() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (InteractEventHandler.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient() && event.getHand() == InteractionHand.MAIN_HAND) {
            InteractEventHandler.onClientMainHandRightClickBlock();
        }
    }
}
