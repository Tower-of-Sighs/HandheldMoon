package cc.sighs.handheldmoon.api.light.impl;

import cc.sighs.handheldmoon.api.light.IRayLightConfig;

/**
 * Default {@link cc.sighs.handheldmoon.api.light.IRayLightConfig} implementation as an immutable record.
 * Created by {@link cc.sighs.handheldmoon.api.light.DynamicLightBuilder#buildConfig()}.
 */
public record RayLightConfigImpl(
        double range,
        double innerAngle,
        double outerAngle,
        double luminance,
        boolean occlusionEnabled,
        LightType type
) implements IRayLightConfig {
}
