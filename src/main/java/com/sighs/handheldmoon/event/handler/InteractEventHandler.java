package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HandheldMoon.MOD_ID, value = Dist.CLIENT)
public class InteractEventHandler {

    @SubscribeEvent
    public static void wheel(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp) {
                if (Minecraft.getInstance().options.keyShift.isDown()) {
                    if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
                        lamp.setXRot(lamp.getXRot() + (float) event.getScrollDeltaY() * 2);
                    } else {
                        lamp.setYRot(lamp.getYRot() + (float) event.getScrollDeltaY() * 2);
                    }
                    HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        MoonlightLampBlockEntity lamp = ClientUtils.getCursorMoonlightLampBlock();
        if (event.getSide().isClient() && event.getHand() == InteractionHand.MAIN_HAND && lamp != null) {
            lamp.setPowered(!lamp.getPowered());
            HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);
        }
    }
}
