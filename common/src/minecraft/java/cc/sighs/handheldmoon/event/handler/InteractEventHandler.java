package cc.sighs.handheldmoon.event.handler;

import cc.sighs.handheldmoon.block.MoonlightLampBlockEntity;
import cc.sighs.handheldmoon.util.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class InteractEventHandler {
    private InteractEventHandler() {
    }

    public static boolean onMouseScroll(double deltaY) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult result) {
            var blockentity = mc.level.getBlockEntity(result.getBlockPos());
            if (blockentity instanceof MoonlightLampBlockEntity lamp && mc.options.keyShift.isDown()) {
                if (result.getDirection() == Direction.UP || result.getDirection() == Direction.DOWN) {
                    lamp.setXRot(lamp.getXRot() + (float) deltaY * 2);
                } else {
                    lamp.setYRot(lamp.getYRot() + (float) deltaY * 2);
                }
                LampInteractionHooks.refresh(lamp);
                return true;
            }
        }
        return false;
    }

    public static void onClientMainHandRightClickBlock() {
        MoonlightLampBlockEntity lamp = ClientUtils.getCursorMoonlightLampBlock();
        if (lamp != null) {
            lamp.setPowered(!lamp.getPowered());
            LampInteractionHooks.refresh(lamp);
        }
    }
}
