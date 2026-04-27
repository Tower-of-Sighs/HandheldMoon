package cc.sighs.handheldmoon.api.light;

import cc.sighs.handheldmoon.api.light.impl.RayLightConfigImpl;
import cc.sighs.handheldmoon.api.light.impl.RayLightBehavior;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;

/**
 * Builder for {@link IRayLightConfig} and its associated
 * {@link RayLightBehavior}.
 * <p>
 * Start with a factory method ({@link #cone()}, {@link #point()},
 * or {@link #area()}), then chain setters, and finally call
 * {@link #build(Supplier, Supplier, BooleanSupplier)} to create a
 * managed light behaviour.
 *
 * <pre>{@code
 * // Player flashlight
 * RayLightBehavior light = DynamicLightBuilder.cone()
 *     .range(24.0).angle(0.5, 0.7)
 *     .luminance(12.0).occlusion(true)
 *     .build(
 *         () -> player.getEyePosition(1.0f),
 *         () -> player.getViewVector(1.0f),
 *         () -> Utils.isUsingFlashlight(player)
 *     );
 *
 * // Block lamp
 * RayLightBehavior lamp = DynamicLightBuilder.cone()
 *     .range(cfg.lightRange()).angle(cfg.innerAngle(), cfg.outerAngle())
 *     .luminance(cfg.luminance()).occlusion(cfg.lightOcclusion())
 *     .build(
 *         () -> pos.getCenter(),
 *         () -> lamp.getViewVec().normalize().scale(-1),
 *         () -> lamp.getPowered()
 *     );
 * }</pre>
 */
public final class DynamicLightBuilder {
    private double range = 24.0;
    private double innerAngle = 0.5;
    private double outerAngle = 0.7;
    private double luminance = 12.0;
    private boolean occlusion = true;
    private final IRayLightConfig.LightType type;

    private DynamicLightBuilder(IRayLightConfig.LightType type) {
        this.type = type;
    }

    // ---- factory methods ----

    /** Create a builder for a directional cone light (flashlight / lamp). */
    public static DynamicLightBuilder cone() {
        return new DynamicLightBuilder(IRayLightConfig.LightType.CONE);
    }

    /**
     * Create a builder for an omni-directional point light.
     * {@code angle()} and {@code innerAngle} / {@code outerAngle} are unused.
     */
    @ApiStatus.Experimental
    public static DynamicLightBuilder point() {
        return new DynamicLightBuilder(IRayLightConfig.LightType.POINT);
    }

    /**
     * Create a builder for a directional area light.
     */
    @ApiStatus.Experimental
    public static DynamicLightBuilder area() {
        return new DynamicLightBuilder(IRayLightConfig.LightType.AREA);
    }

    // ---- setters ----

    /** Maximum light range in blocks. Default: 24. */
    public DynamicLightBuilder range(double range) {
        this.range = range;
        return this;
    }

    /**
     * Set both inner and outer half-angles (in radians).
     * Typical values: {@code inner = 0.5}, {@code outer = 0.7}.
     */
    public DynamicLightBuilder angle(double inner, double outer) {
        this.innerAngle = inner;
        this.outerAngle = outer;
        return this;
    }

    /** Peak luminance. Default: 12. Typical range: 5–15. */
    public DynamicLightBuilder luminance(double luminance) {
        this.luminance = luminance;
        return this;
    }

    /** Enable/disable world occlusion checks. Default: true. */
    public DynamicLightBuilder occlusion(boolean enabled) {
        this.occlusion = enabled;
        return this;
    }

    // ---- build ----

    /**
     * Build an immutable {@link IRayLightConfig} without creating a behaviour.
     * Useful when you only need the configuration object.
     */
    public IRayLightConfig buildConfig() {
        return new RayLightConfigImpl(range, innerAngle, outerAngle, luminance, occlusion, type);
    }

    /**
     * Build a managed {@link RayLightBehavior} that reads position,
     * direction, and active state from the given suppliers each frame.
     *
     * @param position   supplier for the light origin in world space
     * @param direction  supplier for the normalised light direction
     * @param active     supplier for whether the light is currently on
     * @return a new light behaviour (not yet registered with any manager)
     */
    public RayLightBehavior build(
            Supplier<Vec3> position,
            Supplier<Vec3> direction,
            BooleanSupplier active
    ) {
        return new RayLightBehavior(
                buildConfig(), position, direction, active
        );
    }
}
