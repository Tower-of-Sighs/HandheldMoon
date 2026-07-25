package cc.sighs.handheldmoon.fabric;

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
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public final class HandheldMoonFabricClientEvents {
    private HandheldMoonFabricClientEvents() {
    }

    public static void register() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            HandheldMoonClient.onClientTick();
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
                InteractEventHandler.onClientMainHandRightClickBlock();
            }
            return InteractionResult.PASS;
        });

        LevelRenderEvents.END_MAIN.register(context -> {
            Minecraft mc = Minecraft.getInstance();

            RayEvent.renderPlayerViewConesWithRadialGradient(
                    context.poseStack(),
                    context.levelState().cameraRenderState.pos,
                    context.levelState().cameraRenderState.viewRotationMatrix,
                    mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)
            );
        });
        LevelRenderEvents.END_MAIN.register(context -> EffectManager.onLevelRender());

        EntityRenderers.register(ModEntities.MOONLIGHT.get(), FullMoonRenderer::new);
        BlockEntityRenderers.register(ModBlockEntities.MOONLIGHT_LAMP.get(), MoonlightLampRenderer::new);
        ConditionalItemModelProperties.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath(HandheldMoon.MOD_ID, "powered"),
                MoonlightLampPoweredProperty.MAP_CODEC
        );
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
