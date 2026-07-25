package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeOperationEventBridge {
    private NeoForgeOperationEventBridge() {
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        OperationEventHandler.onKey(event.getKey(), event.getAction());
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (OperationEventHandler.onMouseButton(event.getButton(), event.getAction())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (OperationEventHandler.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }
}
