package cc.sighs.handheldmoon.api.light;

import cc.sighs.handheldmoon.api.light.impl.RayLightConfigImpl;

/**
 * Builder for immutable dynamic-light configuration.
 * <p>
 * Start with a factory method ({@link #cone()}, {@link #point()},
 * or {@link #area()}), chain setters, and call {@link #buildConfig()}.
 *
 * <pre>{@code
 * // Player flashlight
 * IRayLightConfig light = DynamicLightBuilder.cone()
 *     .range(24.0).angle(0.5, 0.7)
 *     .luminance(12.0).occlusion(true)
 *     .buildConfig();
 *
 * // Block lamp
 * IRayLightConfig lamp = DynamicLightBuilder.cone()
 *     .range(cfg.lightRange()).angle(cfg.innerAngle(), cfg.outerAngle())
 *     .luminance(cfg.luminance()).occlusion(cfg.lightOcclusion())
 *     .buildConfig();
 * }</pre>
 */
public final class DynamicLightBuilder {
    private double range = 24.0;
    private double innerAngle = 0.5;
    private double outerAngle = 0.7;
    private double luminance = 12.0;
    private AttenuationCurve attenuationCurve = AttenuationCurve.QUADRATIC;
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
    public static DynamicLightBuilder point() {
        return new DynamicLightBuilder(IRayLightConfig.LightType.POINT);
    }

    /**
     * Create a builder for a directional area light.
     */
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

    /** Select the normalized distance falloff preset. Default: quadratic. */
    public DynamicLightBuilder attenuation(AttenuationCurve curve) {
        this.attenuationCurve = curve == null ? AttenuationCurve.QUADRATIC : curve;
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
        return new RayLightConfigImpl(range, innerAngle, outerAngle, luminance,
                attenuationCurve, occlusion, type);
    }

}
