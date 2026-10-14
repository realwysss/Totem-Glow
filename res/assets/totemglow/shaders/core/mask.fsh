#version 330

#moj_import <totemglow:lib.glsl>

uniform sampler2D Sampler0;

in vec2 uv;
in float keep;

out vec4 fragColor;

void main() {
    vec4 c = texture(Sampler0, uv);
    if (c.a < 0.1 || keep < -1.0) {
        discard;
    }
    float e = eyes(c.rgb);
    fragColor = vec4(1.0 - e, e, 1.0, dot(c.rgb, vec3(0.2126, 0.7152, 0.0722)));
}
