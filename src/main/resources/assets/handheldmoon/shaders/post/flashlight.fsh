#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;
uniform float IntensityAmount;
uniform vec2 Offset;
uniform float Radius;
uniform vec2 OutSize;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / OutSize;
    vec4 color = texture(InSampler, uv);
    vec3 finalColor = color.rgb;

    vec2 pixelCoord = gl_FragCoord.xy;
    vec2 center = OutSize * 0.5 + Offset;
    float dist = distance(pixelCoord, center);

    float edge = Radius / 3.0;
    float factor = smoothstep(Radius, Radius - edge, dist);

    if (factor > 0.0) {
        float brightness = dot(finalColor, vec3(0.299, 0.587, 0.114));
        float brightnessResponse = pow(brightness, 3.0) * (1.0 - brightness * 2.0) + brightness * brightness * 2.0;
        float gammaBoost = (1.0 - brightnessResponse) * factor * IntensityAmount * 2.0;
        float gammaAdjust = 1.0 + gammaBoost;
        finalColor = pow(finalColor, vec3(1.0 / gammaAdjust));
        finalColor = clamp(finalColor, 0.0, 1.0);
    }

    fragColor = vec4(finalColor, 1.0);
}