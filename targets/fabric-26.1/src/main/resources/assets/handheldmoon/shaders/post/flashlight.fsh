#version 330

uniform sampler2D InSampler;

layout(std140) uniform FlashlightParams {
    float IntensityAmount;
    vec2 Offset;
    float RadiusRatio;
};

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    vec3 finalColor = color.rgb;

    vec2 texSize = vec2(textureSize(InSampler, 0));
    vec2 pixelCoord = texCoord * texSize;
    vec2 center = texSize * 0.5 + Offset;
    float radius = min(texSize.x, texSize.y) * RadiusRatio;
    float dist = distance(pixelCoord, center);
    float edge = radius / 3.0;
    float factor = smoothstep(radius, radius - edge, dist);

    if (factor > 0.0) {
        float brightness = dot(finalColor, vec3(0.299, 0.587, 0.114));
        float brightnessResponse = pow(brightness, 3.0) * (1.0 - brightness * 2.0) + brightness * brightness * 2.0;
        float gammaBoost = (1.0 - brightnessResponse) * factor * IntensityAmount * 2.0;
        float gammaAdjust = 1.0 + gammaBoost;
        finalColor = pow(finalColor, vec3(1.0 / gammaAdjust));
        finalColor = clamp(finalColor, 0.0, 1.0);
    }

    fragColor = vec4(finalColor, color.a);
}
