#version 330

uniform sampler2D SceneSampler;
uniform sampler2D DeltaSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    // The first pass stores only positive colour deltas.  Sampling the scene
    // again here keeps all world detail at the native framebuffer resolution.
    vec3 delta = texture(DeltaSampler, texCoord).rgb;
    fragColor = vec4(clamp(scene.rgb + delta, 0.0, 1.0), scene.a);
}
