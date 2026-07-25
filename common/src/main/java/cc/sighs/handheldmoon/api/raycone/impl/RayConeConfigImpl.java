package cc.sighs.handheldmoon.api.raycone.impl;

import cc.sighs.handheldmoon.api.raycone.IRayConeConfig;

import java.util.List;

public final class RayConeConfigImpl implements IRayConeConfig {
    private final double range;
    private final double angle;
    private final List<float[]> colorStops;
    private final float[] layerSizeScales;
    private final float[] layerCenterAlphas;
    private final float[] layerEdgeAlphas;
    private final float[][] layerColors;
    private final double noiseAmplitude;
    private final boolean coneRaycast;
    private final FogConfig fog;

    public RayConeConfigImpl(
            double range,
            double angle,
            List<float[]> colorStops,
            float[] layerSizeScales,
            float[] layerCenterAlphas,
            float[] layerEdgeAlphas,
            float[][] layerColors,
            double noiseAmplitude,
            boolean coneRaycast,
            FogConfig fog
    ) {
        this.range = range;
        this.angle = angle;
        this.colorStops = colorStops;
        this.layerSizeScales = layerSizeScales;
        this.layerCenterAlphas = layerCenterAlphas;
        this.layerEdgeAlphas = layerEdgeAlphas;
        this.layerColors = layerColors;
        this.noiseAmplitude = noiseAmplitude;
        this.coneRaycast = coneRaycast;
        this.fog = fog;
    }

    @Override public double range() { return range; }
    @Override public double angle() { return angle; }
    @Override public List<float[]> colorStops() { return colorStops; }
    @Override public int layerCount() { return layerSizeScales.length; }
    @Override public float layerSizeScale(int index) { return layerSizeScales[index]; }
    @Override public float layerCenterAlpha(int index) { return layerCenterAlphas[index]; }
    @Override public float layerEdgeAlpha(int index) { return layerEdgeAlphas[index]; }
    @Override public float[] layerColor(int index) { return layerColors[index]; }
    @Override public double noiseAmplitude() { return noiseAmplitude; }
    @Override public boolean coneRaycast() { return coneRaycast; }
    @Override public FogConfig fog() { return fog; }
}
