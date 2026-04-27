package cc.sighs.handheldmoon.neoforge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.HandheldMoonClient;
import cc.sighs.handheldmoon.client.renderer.FullMoonRenderer;
import cc.sighs.handheldmoon.client.renderer.MoonlightLampRenderer;
import cc.sighs.handheldmoon.client.renderer.item.MoonlightLampPoweredProperty;
import cc.sighs.handheldmoon.event.handler.EffectManager;
import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import cc.sighs.handheldmoon.registry.ModBlockEntities;
import cc.sighs.handheldmoon.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public final class HandheldMoonNeoForgeClientEvents {
    private HandheldMoonNeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        HandheldMoonClient.onClientTick();
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
        boolean canceled = InteractEventHandler.onMouseScroll(event.getScrollDeltaY());
        if (!canceled) {
            canceled = OperationEventHandler.onMouseScroll(event.getScrollDeltaY());
        }
        if (canceled) {
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
    public static void onAfterTranslucentBlocks(RenderLevelStageEvent.AfterWeather event) {
        Minecraft mc = Minecraft.getInstance();

        RayEvent.renderPlayerViewConesWithRadialGradient(
                event.getPoseStack(),
                event.getLevelRenderState().cameraRenderState.pos,
                event.getModelViewMatrix(),
                mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)
        );
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        EffectManager.onLevelRender();
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MOONLIGHT.get(), FullMoonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterConditionalItemProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "powered"), MoonlightLampPoweredProperty.MAP_CODEC);
    }
}
