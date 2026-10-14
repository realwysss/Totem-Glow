/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

import hits.glow.Colors;
import hits.glow.Config;
import hits.glow.Layer;

public record Look(float[] eye, float[] body, float[] tint, float inner, float time, float shimmer) {
    public static final float EYE_GAIN = 2.6f;
    public static final float BODY_GAIN = 1.35f;

    public static Look of(Config c, double seconds) {
        float t = (float)(seconds * (double)c.speed);
        float k = 1.0f;
        float turn = 0.0f;
        float shimmer = 0.0f;
        switch (c.anim) {
            case PULSE: {
                k = 0.62f + 0.38f * (float)Math.sin((double)t * Math.PI * 1.6);
                break;
            }
            case RAINBOW: {
                turn = t * 0.12f;
                break;
            }
            case FLICKER: {
                k = 0.72f + 0.28f * Look.noise(t * 7.0f);
                break;
            }
            case SHIMMER: {
                shimmer = 1.0f;
                break;
            }
        }
        return new Look(Look.pack(c.eyes, k * 2.6f, turn), Look.pack(c.body, k * 1.35f, turn), Look.tint(c.eyes, k, turn), c.inner, t, shimmer);
    }

    private static float[] pack(Layer l, float k, float turn) {
        if (!l.live()) {
            return new float[]{0.0f, 0.0f, 0.0f, 1.0f};
        }
        int c = turn == 0.0f ? l.color : Colors.shift(l.color, turn);
        float s = l.strength * k;
        return new float[]{(float)Colors.r(c) / 255.0f * s, (float)Colors.g(c) / 255.0f * s, (float)Colors.b(c) / 255.0f * s, l.falloff};
    }

    private static float[] tint(Layer l, float k, float turn) {
        if (!l.live()) {
            return new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        }
        int c = turn == 0.0f ? l.color : Colors.shift(l.color, turn);
        float s = 0.6f + 0.4f * Math.min(1.0f, k);
        return new float[]{(float)Colors.r(c) / 255.0f * s, (float)Colors.g(c) / 255.0f * s, (float)Colors.b(c) / 255.0f * s, 1.0f};
    }

    static float noise(float x) {
        int i = (int)Math.floor(x);
        float f = x - (float)i;
        float u = f * f * (3.0f - 2.0f * f);
        return Look.lerp(Look.hash(i), Look.hash(i + 1), u) * 0.6f + Look.lerp(Look.hash(i * 3 + 17), Look.hash(i * 3 + 20), f) * 0.4f;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float hash(int n) {
        n = n << 13 ^ n;
        int m = n * (n * n * 15731 + 789221) + 1376312589;
        return (float)(m & Integer.MAX_VALUE) / 2.1474836E9f;
    }

    public boolean dark() {
        return this.eye[0] + this.eye[1] + this.eye[2] + this.body[0] + this.body[1] + this.body[2] <= 1.0E-4f && this.tint[3] <= 0.0f;
    }
}

