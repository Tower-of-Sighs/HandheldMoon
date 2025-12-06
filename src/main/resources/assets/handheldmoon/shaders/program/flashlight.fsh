#version 150

uniform sampler2D DiffuseSampler;
uniform float IntensityAmount;
uniform vec2 Offset;
uniform float Radius;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(DiffuseSampler, texCoord);
    vec3 finalColor = vec3(color);

    // 屏幕分辨率
    vec2 texSize = textureSize(DiffuseSampler, 0);
    // 坐标
    vec2 pixelCoord = texCoord * texSize;
    // 中心点（屏幕中心 + 偏移量）
    vec2 center = texSize * 0.5 + Offset;
    float dist = distance(pixelCoord, center);

    // 边缘过渡
    float edge = Radius / 3;

    // 平滑过渡
    float factor = smoothstep(Radius, Radius - edge, dist);

    if (factor > 0.0) {
        float brightness = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));
        float brightnessResponse = pow(brightness, 3.0) * (1 - brightness * 2) + brightness * brightness * 2;
        float gammaBoost = (1.0 - brightnessResponse) * factor * IntensityAmount * 2;
        float gammaAdjust = 1.0 + gammaBoost;
        finalColor = pow(finalColor, vec3(1.0 / gammaAdjust));
        finalColor = clamp(finalColor, 0.0, 1.0);
    }

    fragColor = vec4(finalColor, 1.0);
}