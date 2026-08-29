package cc.sighs.handheldmoon.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import cc.sighs.handheldmoon.spi.PlatformServices;

public final class DeviceConfigOpenHelper {
    private DeviceConfigOpenHelper() {
    }

    public static boolean openForCursorTarget() {
        Minecraft mc = Minecraft.getInstance();
        var lampBlock = DeviceConfigTargets.cursorLampBlock(mc);
        if (lampBlock != null) {
            PlatformServices.require().client().openLampConfig(lampBlock);
            return true;
        }
        var fullMoonBlock = DeviceConfigTargets.cursorFullMoonBlock(mc);
        if (fullMoonBlock != null) {
            PlatformServices.require().client().openFullMoonConfig(fullMoonBlock);
            return true;
        }

        var heldLampMain = DeviceConfigTargets.heldLampItem(mc, InteractionHand.MAIN_HAND);
        if (heldLampMain != null) {
            PlatformServices.require().client().openLampConfig(heldLampMain);
            return true;
        }
        var heldFullMoonMain = DeviceConfigTargets.heldFullMoonItem(mc, InteractionHand.MAIN_HAND);
        if (heldFullMoonMain != null) {
            PlatformServices.require().client().openFullMoonConfig(heldFullMoonMain);
            return true;
        }

        var heldLampOff = DeviceConfigTargets.heldLampItem(mc, InteractionHand.OFF_HAND);
        if (heldLampOff != null) {
            PlatformServices.require().client().openLampConfig(heldLampOff);
            return true;
        }
        var heldFullMoonOff = DeviceConfigTargets.heldFullMoonItem(mc, InteractionHand.OFF_HAND);
        if (heldFullMoonOff != null) {
            PlatformServices.require().client().openFullMoonConfig(heldFullMoonOff);
            return true;
        }
        return false;
    }
}
