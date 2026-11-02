#version 150

uniform sampler2D DiffuseSampler;
uniform float IntensityAmount;
uniform vec2 Offset;
uniform float RadiusRatio;

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
    vec2 fromCenter = pixelCoord - center;
    float distSquared = dot(fromCenter, fromCenter);

    // 边缘过渡
    float radius = min(texSize.x, texSize.y) * RadiusRatio;
    if (distSquared >= radius * radius) {
        fragColor = vec4(finalColor, 1.0);
        return;
    }

    float dist = sqrt(distSquared);
    float edge = radius / 3;

    // 平滑过渡
    float factor = smoothstep(radius, radius - edge, dist);

    if (factor > 0.0) {
        float brightness = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));
        float brightnessSquared = brightness * brightness;
        float brightnessResponse = brightnessSquared
                * (brightness - brightnessSquared * 2.0 + 2.0);
        float gammaBoost = (1.0 - brightnessResponse) * factor * IntensityAmount * 2;
        float gammaAdjust = 1.0 + gammaBoost;
        finalColor = pow(finalColor, vec3(1.0 / gammaAdjust));
        finalColor = clamp(finalColor, 0.0, 1.0);
    }

    fragColor = vec4(finalColor, 1.0);
}
