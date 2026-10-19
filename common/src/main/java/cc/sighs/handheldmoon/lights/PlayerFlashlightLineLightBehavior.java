package cc.sighs.handheldmoon.lights;

import cc.sighs.handheldmoon.api.light.DynamicLightBuilder;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;
import cc.sighs.handheldmoon.config.LampDeviceConfig;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightDefaults;
import cc.sighs.handheldmoon.dynamiclight.DynamicLightBehavior;
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
    private LampDeviceConfig config;
    private static final double INNER = 0.5;
    private static final double OUTER = 0.7;

    public PlayerFlashlightLineLightBehavior(Player player) {
        this.player = player;
        this.config = currentConfig(player);
        this.delegate = createDelegate(config);
    }

    private static LampDeviceConfig currentConfig(Player player) {
        return Utils.heldLampConfigOrDefault(player);
    }

    private RayLightBehavior createDelegate(LampDeviceConfig config) {
        // The held flashlight is driven by its own realLightIntensity; the
        // legacy per-device realLight flag only governs placed lamp blocks.
        return new RayLightBehavior(
                DynamicLightBuilder.cone()
                        .range(DynamicLightDefaults.FLASHLIGHT_RANGE)
                        .angle(INNER, OUTER)
                        .luminance(config.realLightIntensity())
                        .occlusion(false)
                        .buildConfig(),
                        () -> player.getEyePosition(1.0f),
                        () -> LineLightMath.computeDirection(player.getYRot(), player.getXRot(), false),
                        () -> Utils.isUsingFlashlight(player)
                                && currentConfig(player).realLightIntensity() > 0,
                        cc.sighs.handheldmoon.registry.Config.flashlightColorArgb(config)
                );
    }

    @Override
    public double lightAt(int blockX, int blockY, int blockZ, double falloffRatio) {
        return delegate.lightAt(blockX, blockY, blockZ, falloffRatio);
    }

    @Override
    public int color() {
        return delegate.color();
    }

    @Override
    public Bounds getBounds() {
        return delegate.getBounds();
    }

    @Override
    public boolean hasChanged() {
        // Colour, luminance and activation all live in the held item's data
        // component, which the ray light config does not observe; rebuild the
        // delegate whenever the per-item flashlight config changes.
        LampDeviceConfig current = currentConfig(player);
        if (!current.equals(config)) {
            config = current;
            delegate = createDelegate(config);
            return true;
        }
        return delegate.hasChanged();
    }

    @Override
    public boolean isRemoved() {
        // Check the live per-item config instead of the delegate's cached
        // activity so raising the intensity back above zero re-registers the
        // light even while the flashlight stayed switched on.
        return !Utils.isUsingFlashlight(player)
                || currentConfig(player).realLightIntensity() <= 0;
    }

    @Override
    public BatchLightSnapshot getBatchLightSnapshot() {
        return delegate.getBatchLightSnapshot();
    }

}
