package cc.sighs.handheldmoon.api.raycone;

import cc.sighs.handheldmoon.api.raycone.impl.RayConeConfigImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Chainable builder for {@link IRayConeConfig}.
 *
 * <pre>{@code
 * IRayConeConfig cfg = RayConeBuilder.create()
 *     .range(24.0).angle(30.0)
 *     .addColorStop("#FFFF00").addColorStop("#FF4400")
 *     .addLayer(1.0f, 0.12f, 0.02f)
 *     .addLayer(0.7f, 0.06f, 0.01f)
 *     .noiseAmplitude(0.15).raycast(true)
 *     .build();
 * }</pre>
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class RayConeBuilder {
    private double range = 24.0;
    private double angle = 30.0;
    private final List<float[]> colorStops = new ArrayList<>();
    private final List<Float> sizeScales = new ArrayList<>();
    private final List<Float> centerAlphas = new ArrayList<>();
    private final List<Float> edgeAlphas = new ArrayList<>();
    private final List<float[]> layerColors = new ArrayList<>();
    private double noiseAmplitude = 0.0;
    private boolean coneRaycast = false;
    private final FogConfigBuilder fogBuilder = new FogConfigBuilder();

    private RayConeBuilder() {
    }

    /** Create a new builder. */
    public static RayConeBuilder create() {
        return new RayConeBuilder();
    }

    // ---- top-level properties ----

    /** Maximum cone length in blocks. Default: 24. */
    public RayConeBuilder range(double range) {
        this.range = range;
        return this;
    }

    /** Full apex angle in degrees. Default: 30. */
    public RayConeBuilder angle(double angle) {
        this.angle = angle;
        return this;
    }

    // ---- color stops ----

    /**
     * Add a colour stop as an ARGB hex string ({@code #RRGGBB} or {@code #AARRGGBB}).
     * Stops are interpolated linearly along the cone length.
     */
    public RayConeBuilder addColorStop(String argbHex) {
        this.colorStops.add(parseHexColor(argbHex));
        return this;
    }

    /**
     * Add a colour stop as an RGB float array {@code {R, G, B}} in [0, 1].
     */
    public RayConeBuilder addColorStop(float r, float g, float b) {
        this.colorStops.add(new float[]{r, g, b});
        return this;
    }

    /** Replace all colour stops. */
    public RayConeBuilder colorStops(List<float[]> stops) {
        this.colorStops.clear();
        if (stops != null) this.colorStops.addAll(stops);
        return this;
    }

    // ---- layers ----

    /**
     * Add a rendering layer.
     *
     * @param sizeScale     radius scale relative to the full cone (1.0 = full angle).
     *                      Smaller values produce tighter inner cones.
     * @param centerAlpha   alpha at the cone apex (0-1).
     * @param edgeAlpha     alpha at the cone base edge (0-1).
     */
    public RayConeBuilder addLayer(float sizeScale, float centerAlpha, float edgeAlpha) {
        this.sizeScales.add(sizeScale);
        this.centerAlphas.add(centerAlpha);
        this.edgeAlphas.add(edgeAlpha);
        this.layerColors.add(null);
        return this;
    }

    /**
     * Add a rendering layer with a fixed colour override.
     *
     * @param sizeScale     radius scale relative to the full cone.
     * @param centerAlpha   alpha at cone apex (0-1).
     * @param edgeAlpha     alpha at cone base edge (0-1).
     * @param colorOverride fixed RGB {@code {R, G, B}} in [0,1],
     *                      or {@code null} to sample from colour stops.
     */
    public RayConeBuilder addLayer(float sizeScale, float centerAlpha, float edgeAlpha, float[] colorOverride) {
        this.sizeScales.add(sizeScale);
        this.centerAlphas.add(centerAlpha);
        this.edgeAlphas.add(edgeAlpha);
        this.layerColors.add(colorOverride);
        return this;
    }

    /** Replace layer parameters. Each list must have the same length. */
    public RayConeBuilder layers(
            List<Float> sizeScales, List<Float> centerAlphas, List<Float> edgeAlphas, List<float[]> colors
    ) {
        this.sizeScales.clear();
        this.centerAlphas.clear();
        this.edgeAlphas.clear();
        this.layerColors.clear();
        if (sizeScales != null) this.sizeScales.addAll(sizeScales);
        if (centerAlphas != null) this.centerAlphas.addAll(centerAlphas);
        if (edgeAlphas != null) this.edgeAlphas.addAll(edgeAlphas);
        if (colors != null) this.layerColors.addAll(colors);
        return this;
    }

    // ---- noise ----

    /**
     * Amplitude of per-vertex colour noise applied along the cone edge.
     * 0 = disabled. Typical range: 0 – 0.3.
     */
    public RayConeBuilder noiseAmplitude(double amplitude) {
        this.noiseAmplitude = amplitude;
        return this;
    }

    // ---- raycast ----

    /**
     * Enable world space raycast clipping. When enabled the cone surface
     * is clipped against world block geometry, creating a volumetric light
     * effect that stops at walls.
     */
    public RayConeBuilder raycast(boolean enabled) {
        this.coneRaycast = enabled;
        return this;
    }

    // ---- fog ----

    /** Begin building the fog sub-configuration. */
    public FogConfigBuilder fog() {
        return fogBuilder;
    }

    /** Apply a complete fog configuration. */
    public RayConeBuilder fog(IRayConeConfig.FogConfig fog) {
        this.fogBuilder.from(fog);
        return this;
    }

    /**
     * Sub-builder for the fog layer. Exit via {@link #end()} to return
     * to the parent builder.
     */
    public final class FogConfigBuilder {
        private boolean enabled = false;
        private double sizeScale = 1.5;
        private double centerAlpha = 0.2;
        private double edgeAlpha = 0.05;
        private float[] color = new float[]{1, 1, 1};

        private FogConfigBuilder() {
        }

        public FogConfigBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /** Radius scale for the fog layer relative to the main cone. */
        public FogConfigBuilder sizeScale(double sizeScale) {
            this.sizeScale = sizeScale;
            return this;
        }

        /** Fog alpha at the cone apex (0-1). */
        public FogConfigBuilder centerAlpha(double centerAlpha) {
            this.centerAlpha = centerAlpha;
            return this;
        }

        /** Fog alpha at the cone base edge (0-1). */
        public FogConfigBuilder edgeAlpha(double edgeAlpha) {
            this.edgeAlpha = edgeAlpha;
            return this;
        }

        /** Fog colour as an ARGB hex string. */
        public FogConfigBuilder color(String argbHex) {
            this.color = parseHexColor(argbHex);
            return this;
        }

        /** Fog colour as RGB float array {@code {R, G, B}} in [0,1]. */
        public FogConfigBuilder color(float[] rgb) {
            this.color = rgb != null ? rgb : new float[]{1, 1, 1};
            return this;
        }

        /** Return to the owning {@link RayConeBuilder}. */
        public RayConeBuilder end() {
            return RayConeBuilder.this;
        }

        private void from(IRayConeConfig.FogConfig other) {
            this.enabled = other.enabled();
            this.sizeScale = other.sizeScale();
            this.centerAlpha = other.centerAlpha();
            this.edgeAlpha = other.edgeAlpha();
            this.color = other.color();
        }
    }

    // ---- build ----

    /** Build the immutable configuration. */
    public IRayConeConfig build() {
        List<float[]> stops = colorStops.isEmpty()
                ? List.of(new float[]{1, 1, 1})
                : List.copyOf(colorStops);
        int count = Math.min(sizeScales.size(), Math.min(centerAlphas.size(), edgeAlphas.size()));
        float[] ss = new float[count];
        float[] ca = new float[count];
        float[] ea = new float[count];
        float[][] lc = new float[count][];
        for (int i = 0; i < count; i++) {
            ss[i] = sizeScales.get(i);
            ca[i] = centerAlphas.get(i);
            ea[i] = edgeAlphas.get(i);
            lc[i] = layerColors.get(i);
        }
        return new RayConeConfigImpl(
                range, angle, stops,
                ss, ca, ea, lc,
                noiseAmplitude, coneRaycast,
                new FogConfigImpl(
                        fogBuilder.enabled, fogBuilder.sizeScale, fogBuilder.centerAlpha,
                        fogBuilder.edgeAlpha, fogBuilder.color
                )
        );
    }

    // ---- internal helpers ----

    static float[] parseHexColor(String s) {
        if (s == null || s.isBlank()) return new float[]{1, 1, 1};
        String t = s.trim();
        if (t.startsWith("#")) t = t.substring(1);
        if (t.length() == 6) t = "FF" + t;
        if (t.length() != 8) return new float[]{1, 1, 1};
        try {
            return new float[]{
                    Integer.parseInt(t.substring(2, 4), 16) / 255f,
                    Integer.parseInt(t.substring(4, 6), 16) / 255f,
                    Integer.parseInt(t.substring(6, 8), 16) / 255f
            };
        } catch (Exception e) {
            return new float[]{1, 1, 1};
        }
    }

    private record FogConfigImpl(
            boolean enabled, double sizeScale, double centerAlpha,
            double edgeAlpha, float[] color
    ) implements IRayConeConfig.FogConfig {
    }
}
