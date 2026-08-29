package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public final class NeoForgeOperationEventBridge {
    private NeoForgeOperationEventBridge() {
    }

    public static void onKey(InputEvent.Key event) {
        OperationEventHandler.onKey(event.getKey(), event.getAction());
    }

    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (OperationEventHandler.onMouseButton(event.getButton(), event.getAction())) {
            event.setCanceled(true);
        }
    }

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (OperationEventHandler.onMouseScroll(event.getScrollDeltaY())) {
            event.setCanceled(true);
        }
    }
}
