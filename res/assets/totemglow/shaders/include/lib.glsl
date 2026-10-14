#version 330

float hue(vec3 c) {
    float hi = max(c.r, max(c.g, c.b));
    float lo = min(c.r, min(c.g, c.b));
    float d = hi - lo;
    if (d < 0.00001) {
        return 0.0;
    }
    float h;
    if (hi == c.r) {
        h = mod((c.g - c.b) / d, 6.0);
    } else if (hi == c.g) {
        h = (c.b - c.r) / d + 2.0;
    } else {
        h = (c.r - c.g) / d + 4.0;
    }
    return h / 6.0;
}

float eyes(vec3 c) {
    float hi = max(c.r, max(c.g, c.b));
    float lo = min(c.r, min(c.g, c.b));
    float s = hi > 0.0 ? (hi - lo) / hi : 0.0;
    float h = hue(c);
    float band = smoothstep(0.19, 0.25, h) * (1.0 - smoothstep(0.72, 0.78, h));
    return band * smoothstep(0.22, 0.38, s) * smoothstep(0.08, 0.2, hi);
}
