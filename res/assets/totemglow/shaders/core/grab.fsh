#version 330

#moj_import <totemglow:lib.glsl>

uniform sampler2D InSampler;

layout(std140) uniform Pass {
    vec4 A;
    vec4 B;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 p = texCoord * B.x - B.y;
    if (p.x < 0.0 || p.y < 0.0 || p.x > 1.0 || p.y > 1.0) {
        fragColor = vec4(0.0);
        return;
    }
    vec4 c = texture(InSampler, A.xy + p * A.zw);
    if (c.a < 0.1) {
        fragColor = vec4(0.0);
        return;
    }
    vec3 rgb = c.rgb / c.a;
    float e = eyes(rgb);
    fragColor = vec4(1.0 - e, e, 1.0, dot(rgb, vec3(0.2126, 0.7152, 0.0722)));
}
