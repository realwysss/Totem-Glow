/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

public final class Colors {
    private Colors() {
    }

    public static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : Math.min(v, hi);
    }

    public static int r(int c) {
        return c >> 16 & 0xFF;
    }

    public static int g(int c) {
        return c >> 8 & 0xFF;
    }

    public static int b(int c) {
        return c & 0xFF;
    }

    public static int rgb(float r, float g, float b) {
        return Math.round(Colors.clamp(r, 0.0f, 1.0f) * 255.0f) << 16 | Math.round(Colors.clamp(g, 0.0f, 1.0f) * 255.0f) << 8 | Math.round(Colors.clamp(b, 0.0f, 1.0f) * 255.0f);
    }

    public static int argb(int alpha, int rgb) {
        return (alpha & 0xFF) << 24 | rgb & 0xFFFFFF;
    }

    public static int alpha(int argb, float k) {
        int a = Math.round((float)(argb >>> 24) * Colors.clamp(k, 0.0f, 1.0f));
        return a << 24 | argb & 0xFFFFFF;
    }

    public static int mix(int a, int b, float t) {
        t = Colors.clamp(t, 0.0f, 1.0f);
        int aa = a >>> 24;
        int ba = b >>> 24;
        return Math.round((float)aa + (float)(ba - aa) * t) << 24 | Math.round((float)Colors.r(a) + (float)(Colors.r(b) - Colors.r(a)) * t) << 16 | Math.round((float)Colors.g(a) + (float)(Colors.g(b) - Colors.g(a)) * t) << 8 | Math.round((float)Colors.b(a) + (float)(Colors.b(b) - Colors.b(a)) * t);
    }

    public static float[] hsv(int c) {
        float r = (float)Colors.r(c) / 255.0f;
        float g = (float)Colors.g(c) / 255.0f;
        float b = (float)Colors.b(c) / 255.0f;
        float hi = Math.max(r, Math.max(g, b));
        float lo = Math.min(r, Math.min(g, b));
        float d = hi - lo;
        float h = 0.0f;
        if (d > 1.0E-6f) {
            h = hi == r ? (g - b) / d % 6.0f : (hi == g ? (b - r) / d + 2.0f : (r - g) / d + 4.0f);
            if ((h /= 6.0f) < 0.0f) {
                h += 1.0f;
            }
        }
        return new float[]{h, hi <= 0.0f ? 0.0f : d / hi, hi};
    }

    public static int fromHsv(float h, float s, float v) {
        h -= (float)Math.floor(h);
        float r = Colors.channel(h, 0.0f);
        float g = Colors.channel(h, 4.0f);
        float b = Colors.channel(h, 2.0f);
        s = Colors.clamp(s, 0.0f, 1.0f);
        v = Colors.clamp(v, 0.0f, 1.0f);
        return Colors.rgb(v * (1.0f - s + s * r), v * (1.0f - s + s * g), v * (1.0f - s + s * b));
    }

    private static float channel(float h, float shift) {
        float k = (h * 6.0f + shift) % 6.0f;
        return Colors.clamp(Math.abs(k - 3.0f) - 1.0f, 0.0f, 1.0f);
    }

    public static int shift(int c, float turns) {
        float[] v = Colors.hsv(c);
        return Colors.fromHsv(v[0] + turns, v[1], v[2]);
    }

    public static String hex(int c) {
        return String.format("#%06X", c & 0xFFFFFF);
    }

    public static int parse(String s, int fallback) {
        if (s == null) {
            return fallback;
        }
        Object t = s.trim();
        if (((String)t).startsWith("#")) {
            t = ((String)t).substring(1);
        }
        if (((String)t).length() == 3) {
            t = "" + ((String)t).charAt(0) + ((String)t).charAt(0) + ((String)t).charAt(1) + ((String)t).charAt(1) + ((String)t).charAt(2) + ((String)t).charAt(2);
        }
        if (((String)t).length() != 6) {
            return fallback;
        }
        try {
            return Integer.parseInt((String)t, 16);
        }
        catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static float luma(int c) {
        return (0.2126f * (float)Colors.r(c) + 0.7152f * (float)Colors.g(c) + 0.0722f * (float)Colors.b(c)) / 255.0f;
    }
}

