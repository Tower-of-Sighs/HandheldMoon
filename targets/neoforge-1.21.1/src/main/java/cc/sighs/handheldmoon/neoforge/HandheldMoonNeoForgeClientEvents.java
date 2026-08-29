package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.ClientRuntime;
import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import cc.sighs.handheldmoon.neoforge.client.HandheldMoonClient;
import cc.sighs.handheldmoon.neoforge.event.handler.EffectManager;
import cc.sighs.handheldmoon.neoforge.event.handler.ShaderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Single NeoForge 1.21.1 client event boundary. */
@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public final class HandheldMoonNeoForgeClientEvents {
    private HandheldMoonNeoForgeClientEvents() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        HandheldMoonClient.registerItemProperties(event);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        ClientRuntime.tick();
        ShaderEventHandler.onClientTick();
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
        boolean consumed = InteractEventHandler.onMouseScroll(event.getScrollDeltaY());
        if (!consumed) {
            consumed = OperationEventHandler.onMouseScroll(event.getScrollDeltaY());
        }
        if (consumed) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isClient() && event.getHand() == InteractionHand.MAIN_HAND) {
            InteractEventHandler.onClientMainHandRightClickBlock();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RayEvent.renderPlayerViewConesWithRadialGradient(
                event.getPoseStack(), event.getCamera().getPosition(), event.getModelViewMatrix(),
                event.getPartialTick().getGameTimeDeltaPartialTick(true));
        EffectManager.onRenderLevelStage(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        HandheldMoonClient.registerRenderer(event);
    }

    @SubscribeEvent
    public static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        HandheldMoonClient.registerModels(event);
    }
}
