package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** State required by the shared entity-backed light calculation. */
public interface FullMoonDynamicLightSource {
    BlockPos getAnchorPos();

    boolean isLampBound();

    int getLampLuminance();

    Vec3 getLightPosition();

    Vec3 getLampDirection();

    LampDeviceConfig getLampConfig();

    FullMoonDeviceConfig getFullMoonConfig();

    boolean isLightRemoved();

    boolean usesEntityBackedLight();
}
