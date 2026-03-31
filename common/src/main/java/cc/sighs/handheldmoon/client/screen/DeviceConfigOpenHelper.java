package cc.sighs.handheldmoon.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public final class DeviceConfigOpenHelper {
    private DeviceConfigOpenHelper() {
    }

    public static boolean openForCursorTarget() {
        Minecraft mc = Minecraft.getInstance();
        var lampBlock = DeviceConfigTargets.cursorLampBlock(mc);
        if (lampBlock != null) {
            mc.setScreen(new MoonlightLampDeviceConfigScreen(lampBlock));
            return true;
        }
        var fullMoonBlock = DeviceConfigTargets.cursorFullMoonBlock(mc);
        if (fullMoonBlock != null) {
            mc.setScreen(new FullMoonDeviceConfigScreen(fullMoonBlock));
            return true;
        }

        var heldLampMain = DeviceConfigTargets.heldLampItem(mc, InteractionHand.MAIN_HAND);
        if (heldLampMain != null) {
            mc.setScreen(new MoonlightLampDeviceConfigScreen(heldLampMain));
            return true;
        }
        var heldFullMoonMain = DeviceConfigTargets.heldFullMoonItem(mc, InteractionHand.MAIN_HAND);
        if (heldFullMoonMain != null) {
            mc.setScreen(new FullMoonDeviceConfigScreen(heldFullMoonMain));
            return true;
        }

        var heldLampOff = DeviceConfigTargets.heldLampItem(mc, InteractionHand.OFF_HAND);
        if (heldLampOff != null) {
            mc.setScreen(new MoonlightLampDeviceConfigScreen(heldLampOff));
            return true;
        }
        var heldFullMoonOff = DeviceConfigTargets.heldFullMoonItem(mc, InteractionHand.OFF_HAND);
        if (heldFullMoonOff != null) {
            mc.setScreen(new FullMoonDeviceConfigScreen(heldFullMoonOff));
            return true;
        }
        return false;
    }
}
