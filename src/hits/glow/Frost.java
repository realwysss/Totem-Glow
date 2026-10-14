/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

import hits.glow.Gpu;

public final class Frost {
    private static final int LEVELS = 3;
    private static final Gpu.Tex[] down = new Gpu.Tex[3];
    private static Gpu.Tex spare;
    private static int width;
    private static int height;
    private static boolean want;
    private static Gpu.Pipe shrink;
    private static Gpu.Pipe blur;
    private static Gpu.Pipe show;
    private static Gpu.Buf[] ubo;

    private Frost() {
    }

    public static void request() {
        want = true;
    }

    public static boolean run() {
        if (!want) {
            return false;
        }
        want = false;
        Frost.setup();
        Gpu.Tex screen = Gpu.screen();
        int w = screen.width();
        int h = screen.height();
        if (w < 16 || h < 16) {
            return false;
        }
        Frost.size(w, h);
        Gpu.Tex src = screen;
        int sw = w;
        int sh = h;
        for (int i = 0; i < 3; ++i) {
            Gpu.write(ubo[i], 1.0f / (float)sw, 1.0f / (float)sh, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            Gpu.draw(shrink, down[i], "Pass", ubo[i], src, null);
            src = down[i];
            sw = Math.max(1, sw >> 1);
            sh = Math.max(1, sh >> 1);
        }
        Gpu.Tex last = down[2];
        float s = 3.2f;
        Gpu.write(ubo[3], 1.0f / (float)sw, 0.0f, 0.0f, 0.0f, s, s, 10.0f, 0.0f);
        Gpu.draw(blur, spare, "Pass", ubo[3], last, null);
        Gpu.write(ubo[4], 0.0f, 1.0f / (float)sh, 0.0f, 0.0f, s, s, 10.0f, 0.0f);
        Gpu.draw(blur, last, "Pass", ubo[4], spare, null);
        Gpu.write(ubo[5], 0.78f, 1.12f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        Gpu.draw(show, screen, "Pass", ubo[5], last, null);
        return true;
    }

    private static void setup() {
        if (shrink != null) {
            return;
        }
        shrink = Gpu.post("down", "Pass", 0, false);
        blur = Gpu.post("blur", "Pass", 0, false);
        show = Gpu.post("frost", "Pass", 0, false);
        ubo = new Gpu.Buf[6];
        for (int i = 0; i < ubo.length; ++i) {
            Frost.ubo[i] = Gpu.ubo("totemglow frost " + i, 32);
        }
    }

    private static void size(int w, int h) {
        if (w == width && h == height && spare != null) {
            return;
        }
        for (int i = 0; i < 3; ++i) {
            if (down[i] != null) {
                down[i].close();
            }
            Frost.down[i] = Gpu.texture("totemglow frost " + i, Math.max(1, w >> i + 1), Math.max(1, h >> i + 1));
        }
        if (spare != null) {
            spare.close();
        }
        spare = Gpu.texture("totemglow frost blur", Math.max(1, w >> 3), Math.max(1, h >> 3));
        width = w;
        height = h;
    }
}

