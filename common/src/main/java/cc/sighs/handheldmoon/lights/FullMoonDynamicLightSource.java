package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightProfileAccess;
import cc.sighs.handheldmoon.api.light.EntityLightRuntimeState;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** State required by the shared entity-backed light calculation. */
public interface FullMoonDynamicLightSource extends EntityLightProfileAccess {
    BlockPos getAnchorPos();

    boolean isLampBound();

    int getLampLuminance();

    Vec3 getLightPosition();

    Vec3 getLampDirection();

    LampDeviceConfig getLampConfig();

    FullMoonDeviceConfig getFullMoonConfig();

    boolean isLightRemoved();

    boolean usesEntityBackedLight();

    /** Returns the effective profile, including any target-side override. */
    @Override
    default EntityLightProfile getLightProfile() {
        if (isLampBound()) {
            LampDeviceConfig config = getLampConfig();
            double outerAngle = config.lightAngle() * 0.5 * Mth.DEG_TO_RAD;
            return EntityLightProfile.cone(
                    config.realLightLuminance(),
                    config.lightRange(),
                    outerAngle * 0.7,
                    outerAngle,
                    config.realLight(),
                    true,
                    config.lightOcclusion(),
                    Vec3.ZERO
            );
        }

        FullMoonDeviceConfig config = getFullMoonConfig();
        return EntityLightProfile.point(
                config.realLightLuminance(),
                18.0,
                config.realLight(),
                config.lightOcclusion(),
                Vec3.ZERO
        );
    }

    /** Returns the current transform and activation state. */
    default EntityLightRuntimeState getLightRuntimeState() {
        boolean enabled = !isLightRemoved() && (!isLampBound() || getLampLuminance() > 0);
        return new EntityLightRuntimeState(enabled, getLightPosition(), getLampDirection());
    }
}
