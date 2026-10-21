package cc.sighs.handheldmoon.dynamiclight;

import org.joml.Vector3fc;

/**
 * Version-independent snapshot of the lightmap render state. The 26.1 targets
 * build this from {@code LightmapRenderState}; older targets simply never call
 * {@link LightmapColorBridge#update}, leaving the tint table empty.
 *
 * @param needsUpdate whether vanilla rebuilt the lightmap this frame
 * @param blockFactor block-light response factor
 * @param skyFactor sky-light response factor
 * @param ambientColor ambient (gamma) color
 * @param nightVisionColor night-vision color
 * @param nightVisionEffectIntensity night-vision strength
 * @param skyLightColor sky-light color
 * @param blockLightTint block-light tint
 * @param bossOverlayWorldDarkening boss-overlay darkening
 * @param darknessEffectScale darkness effect scale
 * @param brightness brightness setting
 */
public record LightmapInputs(
        boolean needsUpdate,
        float blockFactor,
        float skyFactor,
        Vector3fc ambientColor,
        Vector3fc nightVisionColor,
        float nightVisionEffectIntensity,
        Vector3fc skyLightColor,
        Vector3fc blockLightTint,
        float bossOverlayWorldDarkening,
        float darknessEffectScale,
        float brightness
) {
}
