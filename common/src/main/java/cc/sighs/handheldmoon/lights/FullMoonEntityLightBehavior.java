package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.config.FullMoonDeviceConfig;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Shared light calculation for full-moon and lamp entities. */
public final class FullMoonEntityLightBehavior implements DynamicLightBehavior {
    private final FullMoonDynamicLightSource source;
    private RayLightBehavior delegate;
    private boolean lastLampBound;
    private int lastLampLuminance;
    private Object lastDeviceConfig;

    public FullMoonEntityLightBehavior(FullMoonDynamicLightSource source) {
        this.source = source;
        refreshDelegate();
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public Bounds getBounds() {
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        boolean lampBound = source.isLampBound();
        int lampLuminance = source.getLampLuminance();
        Object deviceConfig = currentDeviceConfig();
        if (lampBound != lastLampBound
                || lampLuminance != lastLampLuminance
                || !deviceConfig.equals(lastDeviceConfig)) {
            refreshDelegate();
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        return source.isLightRemoved() || source.getAnchorPos() == null;
    }

    private void refreshDelegate() {
        lastLampBound = source.isLampBound();
        lastLampLuminance = source.getLampLuminance();
        lastDeviceConfig = currentDeviceConfig();
        delegate = lastLampBound ? createLampDelegate((LampDeviceConfig) lastDeviceConfig)
                : createFullMoonDelegate((FullMoonDeviceConfig) lastDeviceConfig);
    }

    private Object currentDeviceConfig() {
        return source.isLampBound() ? source.getLampConfig() : source.getFullMoonConfig();
    }

    private RayLightBehavior createLampDelegate(LampDeviceConfig config) {
        double outerAngle = config.lightAngle() * 0.5 * Mth.DEG_TO_RAD;
        double innerAngle = outerAngle * 0.7;
        double luminance = source.getLampLuminance() > 0 ? config.realLightLuminance() : 0.0;
        return new RayLightBehavior(
                DynamicLightBuilder.cone()
                        .range(DynamicLightDefaults.FLASHLIGHT_RANGE)
                        .angle(innerAngle, outerAngle)
                        .luminance(luminance)
                        .occlusion(config.lightOcclusion())
                        .buildConfig(),
                source::getLightPosition,
                source::getLampDirection,
                () -> config.realLight() && source.getLampLuminance() > 0
        );
    }

    private RayLightBehavior createFullMoonDelegate(FullMoonDeviceConfig config) {
        return new RayLightBehavior(
                DynamicLightBuilder.point()
                        .range(18.0)
                        .luminance(config.realLightLuminance())
                        .occlusion(config.lightOcclusion())
                        .buildConfig(),
                source::getLightPosition,
                () -> Vec3.ZERO,
                config::realLight
        );
    }
}
