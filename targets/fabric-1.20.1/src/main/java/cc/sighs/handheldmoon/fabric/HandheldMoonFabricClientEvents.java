package cc.sighs.handheldmoon.fabric;

import cc.sighs.handheldmoon.client.LightSourceHighlightRenderer;
import cc.sighs.handheldmoon.event.handler.EffectManager;
import cc.sighs.handheldmoon.event.handler.InteractEventHandler;
import cc.sighs.handheldmoon.event.handler.OperationEventHandler;
import cc.sighs.handheldmoon.event.handler.RayEvent;
import cc.sighs.handheldmoon.spi.PlatformServices;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.phys.Vec3;

/** Fabric 1.20.1 client runtime hooks: tick, world rendering and raw input. */
public final class HandheldMoonFabricClientEvents {
    private HandheldMoonFabricClientEvents() {
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                PlatformServices.require().client().tickClient());

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level == null) {
                return;
            }
            PoseStack poseStack = context.matrixStack();
            Vec3 cameraPos = context.camera().getPosition();
            float partialTick = context.tickDelta();
            RayEvent.renderPlayerViewConesWithRadialGradient(
                    poseStack, cameraPos, poseStack.last().pose(), partialTick);
            LightSourceHighlightRenderer.render(
                    poseStack, cameraPos, poseStack.last().pose(), partialTick);
            EffectManager.onRenderLevelStage();
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
                InteractEventHandler.onClientMainHandRightClickBlock();
            }
            return InteractionResult.PASS;
        });
    }

    public static void onKeyInput(int key, int action) {
        OperationEventHandler.onKey(key, action);
    }

    public static boolean onMouseButton(int button, int action) {
        return OperationEventHandler.onMouseButton(button, action);
    }

    public static boolean onMouseScroll(double scrollDeltaY) {
        boolean canceled = InteractEventHandler.onMouseScroll(scrollDeltaY);
        if (!canceled) {
            canceled = OperationEventHandler.onMouseScroll(scrollDeltaY);
        }
        return canceled;
    }
}
