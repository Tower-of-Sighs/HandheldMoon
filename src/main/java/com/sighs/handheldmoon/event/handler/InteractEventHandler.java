package com.sighs.handheldmoon.event.handler;

import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.event.InputEvent;
import com.sighs.handheldmoon.lights.HandheldMoonDynamicLightsInitializer;
import com.sighs.handheldmoon.util.ClientUtils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;


public class InteractEventHandler {
    public static void init() {
        InputEvent.MOUSE_SCROLL.register(InteractEventHandler::wheel);

        UseBlockCallback.EVENT.register(InteractEventHandler::interact);
    }

    public static boolean wheel(double scrollDeltaX, double scrollDeltaY, double mouseX, double mouseY,
                                boolean leftDown, boolean middleDown, boolean rightDown) {
        var mc = Minecraft.getInstance();
        var hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp) {
                if (Minecraft.getInstance().options.keyShift.isDown()) {
                    if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
                        lamp.setXRot(lamp.getXRot() + (float) scrollDeltaY * 2);
                    } else {
                        lamp.setYRot(lamp.getYRot() + (float) scrollDeltaY * 2);
                    }
                    HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);
                    return true;
                }
            }
        }
        return false;
    }

    public static InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide()) {
            var lamp = ClientUtils.getCursorMoonlightLampBlock();
            if (lamp != null) {
                lamp.setPowered(!lamp.getPowered());
                HandheldMoonDynamicLightsInitializer.syncLampBehavior(lamp);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
