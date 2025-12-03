package com.sighs.handheldmoon.event;

import com.sighs.handheldmoon.HandheldMoon;
import com.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import com.sighs.handheldmoon.init.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HandheldMoon.MODID, value = Dist.CLIENT)
public class InteractEvent {
    @SubscribeEvent
    public static void wheel(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp) {
                if (Minecraft.getInstance().options.keyShift.isDown()) {
                    if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
                        lamp.setXRot(lamp.getXRot() + (float) event.getScrollDelta() * 2);
                    } else {
                        lamp.setYRot(lamp.getYRot() + (float) event.getScrollDelta() * 2);
                    }
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
        }
    }
}
