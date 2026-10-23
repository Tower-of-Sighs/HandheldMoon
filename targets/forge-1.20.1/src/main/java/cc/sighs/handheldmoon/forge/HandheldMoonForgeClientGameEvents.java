package cc.sighs.handheldmoon.forge;

import cc.sighs.handheldmoon.HandheldMoon;
import cc.sighs.handheldmoon.client.LightSourceHighlightRenderer;
import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import cc.sighs.handheldmoon.event.handler.EffectManager;
import cc.sighs.handheldmoon.spi.PlatformServices;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/** Forge-bus client runtime events for Forge 1.20.1. */
@Mod.EventBusSubscriber(modid = HandheldMoon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class HandheldMoonForgeClientGameEvents {
    private HandheldMoonForgeClientGameEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            PlatformServices.require().client().tickClient();
        }
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
        boolean consumed = InteractEventHandler.onMouseScroll(event.getScrollDelta());
        if (!consumed) {
            consumed = OperationEventHandler.onMouseScroll(event.getScrollDelta());
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();
        float partialTick = event.getPartialTick();
        RayEvent.renderPlayerViewConesWithRadialGradient(
                poseStack, event.getCamera().getPosition(), poseStack.last().pose(), partialTick);
        LightSourceHighlightRenderer.render(
                poseStack, event.getCamera().getPosition(), poseStack.last().pose(), partialTick);
        EffectManager.onRenderLevelStage(event);
    }
}
