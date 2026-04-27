package cc.sighs.handheldmoon.api.raycone;

import java.util.List;

/**
 * Configuration for a ray cone rendering layer.
 * <p>
 * Each cone can have multiple layers (for additive blending), a color gradient
 * along its length, noise-based color variation, optional world-space raycast
 * clipping, and a secondary fog layer.
 * <p>
 * Create instances via {@link RayConeBuilder}.
 *
 * <pre>{@code
 * IRayConeConfig cfg = RayConeBuilder.create()
 *     .range(24.0)
 *     .angle(30.0)
 *     .addColorStop("#FFFF00")
 *     .addColorStop("#FF4400")
 *     .addLayer(1.0f, 0.12f, 0.02f)
 *     .addLayer(0.7f, 0.06f, 0.01f)
 *     .noiseAmplitude(0.15)
 *     .raycast(true)
 *     .build();
 * }</pre>
 */
public interface IRayConeConfig {
    /** Maximum cone length in blocks. */
    double range();

    /** Full apex angle in degrees. */
    double angle();

    /**
     * Color gradient stops along the cone length (apex → base).
     * Each element is an RGB float array {@code {R, G, B}} in [0, 1].
     */
    List<float[]> colorStops();

    /** Number of blended layers. */
    int layerCount();

    /** Per-layer radius scale (1.0 = full angle). */
    float layerSizeScale(int index);

    /** Per-layer alpha at the cone apex. */
    float layerCenterAlpha(int index);

    /** Per-layer alpha at the cone base edge. */
    float layerEdgeAlpha(int index);

    /**
     * Per-layer fixed RGB colour, or {@code null} to sample from {@link #colorStops()}.
     * Returned array should not be modified.
     */
    float[] layerColor(int index);

    /**
     * Amplitude of per-vertex colour noise applied to the cone edge.
     * 0 = disabled.
     */
    double noiseAmplitude();

    /** Whether cone surface intersects world blocks (raycast clip). */
    boolean coneRaycast();

    /** Secondary fog-layer configuration. */
    FogConfig fog();

    /**
     * Fog layer rendered on top of the cone layers.
     * <p>
     * Configured via {@link RayConeBuilder#fog()}.
     */
    interface FogConfig {
        boolean enabled();
        double sizeScale();
        double centerAlpha();
        double edgeAlpha();
        float[] color();
    }
}
