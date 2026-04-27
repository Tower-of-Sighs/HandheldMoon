package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.registry.Config;
import cc.sighs.handheldmoon.util.LineLightMath;
import cc.sighs.handheldmoon.util.Utils;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
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
    private double lastRange;
    private double lastLuminance;
    private boolean lastOcclusion;

    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;

    public PlayerFlashlightLineLightBehavior(Player player) {
        this.player = player;
        this.delegate = createDelegate();
        this.lastRange = Config.LIGHT_RANGE.get();
        this.lastLuminance = Config.REAL_LIGHT_LUMINANCE.get();
        this.lastOcclusion = Config.LIGHT_OCCLUSION.get();
    }

    private RayLightBehavior createDelegate() {
        return DynamicLightBuilder.cone()
                .range(Config.LIGHT_RANGE.get())
                .angle(INNER, OUTER)
                .luminance(Config.REAL_LIGHT_LUMINANCE.get())
                .occlusion(Config.LIGHT_OCCLUSION.get())
                .build(
                        () -> player.getEyePosition(1.0f),
                        () -> LineLightMath.computeDirection(player.getYRot(), player.getXRot(), false),
                        () -> Utils.isUsingFlashlight(player)
                );
    }

    @Override
    public double lightAtPos(BlockPos query, double falloffRatio) {
        return delegate.lightAtPos(query, falloffRatio);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return delegate.getBoundingBox();
    }

    @Override
    public boolean hasChanged() {
        boolean cfgChanged = Math.abs(Config.LIGHT_RANGE.get() - lastRange) > 0.001
                || Math.abs(Config.REAL_LIGHT_LUMINANCE.get() - lastLuminance) > 0.001
                || Config.LIGHT_OCCLUSION.get() != lastOcclusion;
        if (cfgChanged) {
            lastRange = Config.LIGHT_RANGE.get();
            lastLuminance = Config.REAL_LIGHT_LUMINANCE.get();
            lastOcclusion = Config.LIGHT_OCCLUSION.get();
            delegate = createDelegate();
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        return delegate.isRemoved();
    }
}
