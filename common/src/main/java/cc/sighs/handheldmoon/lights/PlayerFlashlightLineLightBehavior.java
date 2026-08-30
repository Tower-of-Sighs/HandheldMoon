package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.AttenuationCurve;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.LineLightMath;
import cc.sighs.handheldmoon.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Player flashlight light behavior.
 * <p>
 * Delegates to {@link RayLightBehavior} internally via
 * the {@link cc.sighs.handheldmoon.api.light} API.
 */
public class PlayerFlashlightLineLightBehavior implements DynamicLightBehavior {
    private final Player player;
    private RayLightBehavior delegate;
    private double lastLuminance;
    private boolean lastOcclusion;
    private double lastRange;
    private double lastAngle;
    private AttenuationCurve lastAttenuation;

    public PlayerFlashlightLineLightBehavior(Player player) {
        this.player = player;
        this.delegate = createDelegate();
        this.lastLuminance = Config.REAL_LIGHT_LUMINANCE.get();
        this.lastOcclusion = Config.LIGHT_OCCLUSION.get();
        this.lastRange = Config.REAL_LIGHT_RADIUS.get();
        this.lastAngle = Config.LIGHT_ANGLE.get();
        this.lastAttenuation = currentAttenuation();
    }

    private RayLightBehavior createDelegate() {
        double outerAngle = Config.LIGHT_ANGLE.get() * 0.5 * net.minecraft.util.Mth.DEG_TO_RAD;
        return new RayLightBehavior(
                DynamicLightBuilder.cone()
                        .range(Config.REAL_LIGHT_RADIUS.get())
                        .angle(outerAngle * 0.7, outerAngle)
                        .luminance(Config.REAL_LIGHT_LUMINANCE.get())
                        .attenuation(currentAttenuation())
                        .occlusion(Config.LIGHT_OCCLUSION.get())
                        .buildConfig(),
                        () -> player.getEyePosition(1.0f),
                        () -> LineLightMath.computeDirection(player.getYRot(), player.getXRot(), false),
                        () -> Utils.isUsingFlashlight(player)
                );
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
        boolean cfgChanged = Math.abs(Config.REAL_LIGHT_LUMINANCE.get() - lastLuminance) > 0.001
                || Config.LIGHT_OCCLUSION.get() != lastOcclusion
                || Math.abs(Config.REAL_LIGHT_RADIUS.get() - lastRange) > 0.001
                || currentAttenuation() != lastAttenuation
                || Math.abs(Config.LIGHT_ANGLE.get() - lastAngle) > 0.001;
        if (cfgChanged) {
            lastLuminance = Config.REAL_LIGHT_LUMINANCE.get();
            lastOcclusion = Config.LIGHT_OCCLUSION.get();
            lastRange = Config.REAL_LIGHT_RADIUS.get();
            lastAttenuation = currentAttenuation();
            lastAngle = Config.LIGHT_ANGLE.get();
            delegate = createDelegate();
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        return delegate.isRemoved();
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate.getBatchLightSnapshot();
    }

    private static AttenuationCurve currentAttenuation() {
        return AttenuationCurve.parse(Config.REAL_LIGHT_ATTENUATION.get());
    }
}
