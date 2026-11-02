package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.EntityLightProfile;
import cc.sighs.handheldmoon.api.light.EntityLightProfileAccess;
import cc.sighs.handheldmoon.api.light.EntityLightRuntimeState;
import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.ColorUtils;
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

    /** Identifier of the item or system that created this light source. */
    default String getSourceId() {
        return "";
    }

    boolean isLightRemoved();

    boolean usesEntityBackedLight();

    /**
     * Returns the profile derived from the current device configuration.
     * <p>
     * Target entities copy this value into their synchronized entity data;
     * the light calculation itself then consumes only that entity-facing
     * profile channel.
     */
    default EntityLightProfile getDefaultLightProfile() {
        if (isLampBound()) {
            LampDeviceConfig config = getLampConfig();
            double outerAngle = config.lightAngle() * 0.5 * Mth.DEG_TO_RAD;
            // A placed lamp behaves exactly like the held flashlight: same real
            // light level and the same lens-tinted color (alpha = tint strength).
            String lightColor = ColorUtils.argbToWebColor(Config.flashlightColorArgb(config));
            return EntityLightProfile.cone(
                    config.realLightIntensity(),
                    DynamicLightDefaults.FLASHLIGHT_RANGE,
                    outerAngle * 0.7,
                    outerAngle,
                    config.realLight() && config.realLightIntensity() > 0,
                    config.enableVisibleCone(),
                    config.lightOcclusion(),
                    Vec3.ZERO,
                    AttenuationCurve.QUADRATIC,
                    lightColor
            );
        }

        FullMoonDeviceConfig config = getFullMoonConfig();
        return EntityLightProfile.point(
                config.realLightLuminance(),
                DynamicLightDefaults.FULL_MOON_RANGE,
                config.realLight(),
                config.lightOcclusion(),
                Vec3.ZERO,
                AttenuationCurve.QUADRATIC
        );
    }

    /** Returns the effective profile, including any target-side override. */
    @Override
    default EntityLightProfile getLightProfile() {
        return getDefaultLightProfile();
    }

    /** Returns the current transform and activation state. */
    default EntityLightRuntimeState getLightRuntimeState() {
        boolean enabled = !isLightRemoved() && (!isLampBound() || getLampLuminance() > 0);
        return new EntityLightRuntimeState(enabled, getLightPosition(), getLampDirection());
    }
}
