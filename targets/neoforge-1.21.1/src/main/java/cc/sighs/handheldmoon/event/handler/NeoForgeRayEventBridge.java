package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.HandheldMoon;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeRayEventBridge {
    private NeoForgeRayEventBridge() {
    }

    @SubscribeEvent
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
