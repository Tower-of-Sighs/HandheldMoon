package cc.sighs.handheldmoon.api.light;

import java.util.Locale;

/**
 * Normalized distance falloff presets for real entity-backed lights.
 *
 * <p>The input to {@link #at(double)} is normalized distance in the range
 * {@code [0, 1]}. Every preset is one at the source and exactly zero at the
 * configured radius. The final light value is {@code peak * at(distance / radius)}.</p>
 */
public enum AttenuationCurve {
    /** Constant linear decrease from peak to zero. */
    LINEAR {
        @Override
        protected double sample(double t) {
            return 1.0 - t;
        }
    },
    /** Existing quadratic profile, {@code 1 - t^2}. */
    QUADRATIC {
        @Override
        protected double sample(double t) {
            return 1.0 - t * t;
        }
    },
    /** Exponential decrease normalized so the radius is still exactly zero. */
    EXPONENTIAL {
        @Override
        protected double sample(double t) {
            return (Math.exp(-EXPONENTIAL_K * t) - EXPONENTIAL_END) / EXPONENTIAL_DENOM;
        }
    },
    /** Logarithmic decrease normalized so the radius is exactly zero. */
    LOGARITHMIC {
        @Override
        protected double sample(double t) {
            return 1.0 - Math.log1p(LOGARITHMIC_K * t) / LOGARITHMIC_DENOM;
        }
    },
    /** Constant peak intensity inside the radius, with a hard outer edge. */
    NONE {
        @Override
        protected double sample(double t) {
            return 1.0;
        }
    };

    private static final double EXPONENTIAL_K = 4.0;
    private static final double EXPONENTIAL_END = Math.exp(-EXPONENTIAL_K);
    private static final double EXPONENTIAL_DENOM = 1.0 - EXPONENTIAL_END;
    private static final double LOGARITHMIC_K = 4.0;
    private static final double LOGARITHMIC_DENOM = Math.log1p(LOGARITHMIC_K);

    /** Applies the curve to normalized distance, clamping all edge cases. */
    public final double at(double normalizedDistance) {
        if (!Double.isFinite(normalizedDistance) || normalizedDistance >= 1.0) {
            return 0.0;
        }
        if (normalizedDistance <= 0.0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, sample(normalizedDistance)));
    }

    protected abstract double sample(double normalizedDistance);

    public static AttenuationCurve parse(String value) {
        if (value == null) {
            return QUADRATIC;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("EXP") || normalized.equals("EXPONENTIAL_DECAY")) {
            return EXPONENTIAL;
        }
        if (normalized.equals("LOG") || normalized.equals("LOGARITHM")) {
            return LOGARITHMIC;
        }
        if (normalized.equals("NO_DECAY") || normalized.equals("CONSTANT")) {
            return NONE;
        }
        try {
            return valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return QUADRATIC;
        }
    }
}
