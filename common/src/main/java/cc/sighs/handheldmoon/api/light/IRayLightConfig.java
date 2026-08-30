package cc.sighs.handheldmoon.api.light;

/**
 * Configuration for a dynamic ray light source.
 * <p>
 * Created via {@link DynamicLightBuilder}. Each light type (cone, point, area)
 * may interpret or ignore certain parameters; see the builder factory method
 * documentation for details.
 *
 * <pre>{@code
 * IRayLightConfig cfg = DynamicLightBuilder.cone()
 *     .range(24.0).angle(0.5, 0.7)
 *     .luminance(12.0).occlusion(true)
 *     .build();
 * }</pre>
 */
public interface IRayLightConfig {

    /** Maximum light range in blocks. */
    double range();

    /**
     * Inner cone half-angle in radians.
     * <p>
     * Full brightness within this angle, then smooth falloff to outer.
     * For point lights this is unused.
     */
    double innerAngle();

    /**
     * Outer cone half-angle in radians.
     * <p>
     * Light level reaches zero at this angle.
     * For point lights this is unused.
     */
    double outerAngle();

    /** Peak luminance (arbitrary unit, typical range 5–15). */
    double luminance();

    /** Distance falloff preset used between the source and {@link #range()}. */
    default AttenuationCurve attenuationCurve() {
        return AttenuationCurve.QUADRATIC;
    }

    /** Enable world-space occlusion raycast checks. */
    boolean occlusionEnabled();

    /** The type of this light source. */
    LightType type();

    /** Supported light source shapes. */
    enum LightType {
        /** Directional cone (flashlight, lamp). */
        CONE,
        /** Omni-directional point light. */
        POINT,
        /** Directional area light. */
        AREA
    }
}
