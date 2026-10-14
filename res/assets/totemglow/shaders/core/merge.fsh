#version 330

uniform sampler2D InSampler;
uniform sampler2D MaskSampler;

layout(std140) uniform Merge {
    vec4 Eye;
    vec4 Body;
    vec4 Look;
    vec4 Size;
    vec4 Tint;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec3 b = clamp(texture(InSampler, texCoord).rgb, 0.0, 1.0);
    vec4 m = texture(MaskSampler, texCoord);
    vec3 g = Body.rgb * pow(b.r, Body.a) * mix(1.0 - m.b, 1.0, Look.x) + Eye.rgb * pow(b.g, Eye.a);
    if (Look.z > 0.0) {
        vec2 p = texCoord * Size.xy / Size.y;
        float band = pow(0.5 + 0.5 * sin((p.x * 0.8 + p.y) * Size.z - Look.y * 3.2), 10.0);
        g *= 1.0 + Look.z * band * 2.2;
    }
    g = 1.0 - exp(-g);
    float e = clamp(m.g * Tint.a, 0.0, 1.0);
    vec3 eye = Tint.rgb * (0.35 + 0.85 * m.a);
    vec3 rgb = min(mix(g, eye + g * 0.35, e), vec3(1.0));
    float a = max(e, Look.w * max(g.r, max(g.g, g.b)));
    fragColor = vec4(rgb, a);
}
