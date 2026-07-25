package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;

public final class RayLightConfigImpl implements IRayLightConfig {
    private final double range;
    private final double innerAngle;
    private final double outerAngle;
    private final double luminance;
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
        this.range = range;
        this.innerAngle = innerAngle;
        this.outerAngle = outerAngle;
        this.luminance = luminance;
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
    public boolean occlusionEnabled() {
        return occlusionEnabled;
    }

    @Override
    public LightType type() {
        return type;
    }
}
