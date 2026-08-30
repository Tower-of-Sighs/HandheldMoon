#version 330

uniform sampler2D InSampler;

layout(std140) uniform FlashlightParams {
    float IntensityAmount;
    vec2 Offset;
    float RadiusRatio;
};

in vec2 texCoord;
out vec4 fragColor;

// PostPass fills this block for every input/output pair.  Using it avoids a
// textureSize query in the hot fragment path and also remains correct when the
// intermediate target is rendered at a reduced resolution.
layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

void main() {
    vec2 texSize = InSize;
    vec2 pixelCoord = texCoord * texSize;
    vec2 center = texSize * 0.5 + Offset;
    float radius = min(texSize.x, texSize.y) * RadiusRatio;
    vec2 fromCenter = pixelCoord - center;
    float distSquared = dot(fromCenter, fromCenter);
    float radiusSquared = radius * radius;
    // Outside the additive cone the result is always zero. Rejecting those
    // pixels before sampling the scene avoids work across most of the screen.
    if (distSquared >= radiusSquared) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 color = texture(InSampler, texCoord);
    float dist = sqrt(distSquared);
    float edge = radius / 3.0;
    float factor = smoothstep(radius, radius - edge, dist);

    float brightness = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    // Multiplication is exactly equivalent to pow(brightness, 3.0), but
    // avoids dispatching the general-purpose power operation.
    float brightnessSquared = brightness * brightness;
    float brightnessResponse = brightnessSquared
            * (brightness - brightnessSquared * 2.0 + 2.0);
    float gammaBoost = (1.0 - brightnessResponse) * factor * IntensityAmount * 2.0;
    float gammaAdjust = 1.0 + gammaBoost;
    vec3 correctedColor = pow(color.rgb, vec3(1.0 / gammaAdjust));
    correctedColor = clamp(correctedColor, 0.0, 1.0);
    // Gamma correction can very slightly darken near-white pixels. The
    // low-resolution pass is an additive mask, so discard negative deltas
    // instead of encoding signed values into an 8-bit render target.
    vec3 delta = max(correctedColor - color.rgb, vec3(0.0));

    fragColor = vec4(delta, 1.0);
}
