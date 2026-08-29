package cc.sighs.handheldmoon.neoforge.event.handler;

import cc.sighs.handheldmoon.event.handler.RayEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class NeoForgeRayEventBridge {
    private NeoForgeRayEventBridge() {
    }

    public static void renderCones(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        RayEvent.renderPlayerViewConesWithRadialGradient(
                event.getPoseStack(),
                event.getCamera().getPosition(),
                event.getModelViewMatrix(),
                event.getPartialTick().getGameTimeDeltaPartialTick(true)
        );
    }
}
