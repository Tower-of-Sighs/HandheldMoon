#version 150

in vec4 vertexColor;
in vec2 texCoord0;

uniform vec3 uApex;
uniform vec3 uDir;
uniform float uRange;
uniform float uHalfAngle;

uniform float uSizeScale;
uniform float uCenterAlpha;
uniform float uEdgeAlpha;

uniform float uNoiseAmp;
uniform float uUseOverride;
uniform vec3 uOverrideColor;

uniform float uPalCount;
uniform vec3 uPal0;
uniform vec3 uPal1;
uniform vec3 uPal2;
uniform vec3 uPal3;

out vec4 fragColor;

vec3 paletteColor(float t) {
    int count = int(uPalCount + 0.5);
    if (count <= 0) return vec3(1.0);
    vec3 p0 = uPal0, p1 = uPal1, p2 = uPal2, p3 = uPal3;
    if (count == 1) return p0;
    float pos = t * float(count - 1);
    int i0 = int(floor(pos));
    int i1 = min(count - 1, i0 + 1);
    float w = pos - float(i0);
    vec3 a = (i0 == 0 ? p0 : i0 == 1 ? p1 : i0 == 2 ? p2 : p3);
    vec3 b = (i1 == 0 ? p0 : i1 == 1 ? p1 : i1 == 2 ? p2 : p3);
    return mix(a, b, w);
}

float noise3(float x) {
    return 0.5 + 0.25 * sin(x * 7.23) + 0.25 * sin(x * 13.69) + 0.10 * sin(x * 19.41);
}

void main() {
    float t = texCoord0.x;
    float thetaN = texCoord0.y;

    float n = noise3(thetaN);
    float tn = clamp(t + (n - 0.5) * 2.0 * uNoiseAmp, 0.0, 1.0);

    vec3 baseColor = (uUseOverride > 0.5) ? uOverrideColor : paletteColor(tn);

    float alpha = mix(uCenterAlpha, uEdgeAlpha, t);
    float alphaNoise = 0.85 + 0.15 * (0.5 + 0.5 * sin(thetaN * 11.0));
    alpha *= alphaNoise;

    fragColor = vec4(baseColor, alpha) * vertexColor;
}