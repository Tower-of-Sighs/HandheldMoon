package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class InteractEventHandler {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        var mc = Minecraft.getInstance();

        // 确保世界和玩家存在
        if (mc.level == null || mc.player == null) return;

        var hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockEntity = mc.level.getBlockEntity(result.getBlockPos());

            if (blockEntity instanceof MoonlightLampBlockEntity lamp) {
                if (mc.options.keyShift.isDown()) {
                    double scrollDeltaY = event.getScrollDeltaY();

                    if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
                        lamp.setXRot(lamp.getXRot() + (float) scrollDeltaY * 2);
                    } else {
                        lamp.setYRot(lamp.getYRot() + (float) scrollDeltaY * 2);
                    }

                    HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);

                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            var lamp = ClientUtils.getCursorMoonlightLampBlock();

            if (lamp != null) {
                lamp.setPowered(!lamp.getPowered());
                HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);

                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.SUCCESS);
            }
        }
    }
}