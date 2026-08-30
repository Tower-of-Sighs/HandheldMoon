package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;
import cc.sighs.handheldmoon.api.light.AttenuationCurve;

public final class RayLightConfigImpl implements IRayLightConfig {
    private final double range;
    private final double innerAngle;
    private final double outerAngle;
    private final double luminance;
    private final AttenuationCurve attenuationCurve;
    private final boolean occlusionEnabled;
    private final LightType type;

    public RayLightConfigImpl(
            double range,
            double innerAngle,
            double outerAngle,
            double luminance,
            boolean occlusionEnabled,
            LightType type
    ) {
        this(range, innerAngle, outerAngle, luminance, AttenuationCurve.QUADRATIC,
                occlusionEnabled, type);
    }

    public RayLightConfigImpl(
            double range,
            double innerAngle,
            double outerAngle,
            double luminance,
            AttenuationCurve attenuationCurve,
            boolean occlusionEnabled,
            LightType type
    ) {
        this.range = range;
        this.innerAngle = innerAngle;
        this.outerAngle = outerAngle;
        this.luminance = luminance;
        this.attenuationCurve = attenuationCurve == null ? AttenuationCurve.QUADRATIC : attenuationCurve;
        this.occlusionEnabled = occlusionEnabled;
        this.type = type;
    }

    @Override
    public double range() {
        return range;
    }

    @Override
    public double innerAngle() {
        return innerAngle;
    }

    @Override
    public double outerAngle() {
        return outerAngle;
    }

    @Override
    public double luminance() {
        return luminance;
    }

    @Override
    public AttenuationCurve attenuationCurve() {
        return attenuationCurve;
    }

    @Override
    public boolean occlusionEnabled() {
        return occlusionEnabled;
    }

    @Override
    public LightType type() {
        return type;
    }
}
