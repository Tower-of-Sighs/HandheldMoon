package cc.sighs.handheldmoon.api.raycone.impl;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;

import java.util.List;

/**
 * Default {@link IRayConeConfig} implementation as an immutable record.
 * Created by {@link cc.sighs.handheldmoon.api.raycone.RayConeBuilder#build()}.
 */
public record RayConeConfigImpl(
        double range,
        double angle,
        List<float[]> colorStops,
        float[] layerSizeScales,
        float[] layerCenterAlphas,
        float[] layerEdgeAlphas,
        float[][] layerColors,
        double noiseAmplitude,
        boolean coneRaycast,
        IRayConeConfig.FogConfig fog
) implements IRayConeConfig {
    @Override
    public int layerCount() {
        return layerSizeScales.length;
    }

    @Override
    public float layerSizeScale(int index) {
        return layerSizeScales[index];
    }

    @Override
    public float layerCenterAlpha(int index) {
        return layerCenterAlphas[index];
    }

    @Override
    public float layerEdgeAlpha(int index) {
        return layerEdgeAlphas[index];
    }

    @Override
    public float[] layerColor(int index) {
        return layerColors[index];
    }
}
