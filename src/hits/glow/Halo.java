/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

import hits.glow.Config;
import hits.glow.Glow;
import hits.glow.Gpu;
import hits.glow.Look;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class Halo {
    private static final float SPREAD = 1.0f;
    private static final Map<Object, Slot> SLOTS = new HashMap<Object, Slot>();
    private static long frame;
    private static Gpu.Pipe grab;
    private static Gpu.Pipe blur;
    private static Gpu.Pipe merge;
    private static Gpu.Pipe blit;
    private static Gpu.Buf grabUbo;
    private static Gpu.Buf blurX;
    private static Gpu.Buf blurY;
    private static Gpu.Buf mergeUbo;

    private Halo() {
    }

    public static void tick() {
        ++frame;
        if (SLOTS.isEmpty()) {
            return;
        }
        Iterator<Slot> it = SLOTS.values().iterator();
        while (it.hasNext()) {
            Slot s = it.next();
            if (frame - s.seen <= 120L) continue;
            s.close();
            it.remove();
        }
    }

    public static boolean want() {
        Config c = Config.get();
        return c.live() && c.gui;
    }

    public static Gpu.Pipe pipeline() {
        Halo.setup();
        return blit;
    }

    public static Result bake(Gpu.Tex atlas, float u0, float v0, float u1, float v1, Object key) {
        Config c = Config.get();
        Look look = Look.of(c, Glow.seconds());
        if (look.dark()) {
            return null;
        }
        Halo.setup();
        int scale = Math.max(1, Gpu.guiScale());
        float sb = c.body.live() ? Math.max(0.05f, c.body.radius * 1.0f) : 0.05f;
        float se = c.eyes.live() ? Math.max(0.05f, c.eyes.radius * 1.0f) : 0.05f;
        float top = Math.max(sb, se);
        int pad = Math.min(28, (int)Math.ceil(top * 3.0f) + 1);
        int size = (16 + 2 * pad) * scale;
        Slot s = SLOTS.get(key);
        if (s == null || s.size != size) {
            if (s != null) {
                s.close();
            }
            s = new Slot(size, pad);
            SLOTS.put(key, s);
        }
        s.seen = frame;
        if (s.baked == frame) {
            return s.result;
        }
        s.baked = frame;
        float span = (16.0f + 2.0f * (float)pad) / 16.0f;
        Gpu.write(grabUbo, u0, v0, u1 - u0, v1 - v0, span, (float)pad / 16.0f, 0.0f, 0.0f);
        Gpu.draw(grab, s.a, "Pass", grabUbo, atlas, null);
        float bs = sb * (float)scale;
        float es = se * (float)scale;
        float taps = (float)Math.min(96.0, Math.ceil(Math.max(bs, es) * 3.0f));
        Gpu.write(blurX, 1.0f / (float)size, 0.0f, 0.0f, 0.0f, bs, es, taps, 0.0f);
        Gpu.draw(blur, s.c, "Pass", blurX, s.a, null);
        Gpu.write(blurY, 0.0f, 1.0f / (float)size, 0.0f, 0.0f, bs, es, taps, 0.0f);
        Gpu.draw(blur, s.b, "Pass", blurY, s.c, null);
        Halo.merge(mergeUbo, look, size, size, 6.0f);
        Gpu.draw(merge, s.c, "Merge", mergeUbo, s.b, s.a);
        return s.result;
    }

    static void merge(Gpu.Buf ubo, Look look, float w, float h, float bands) {
        float[] e = look.eye();
        float[] b = look.body();
        float[] t = look.tint();
        Gpu.write(ubo, e[0], e[1], e[2], e[3], b[0], b[1], b[2], b[3], look.inner(), look.time(), look.shimmer(), Config.get().screen ? 1.0f : 0.0f, w, h, bands, 0.0f, t[0], t[1], t[2], t[3]);
    }

    private static void setup() {
        if (grab != null) {
            return;
        }
        grab = Gpu.post("grab", "Pass", 0, false);
        blur = Gpu.post("blur", "Pass", 0, false);
        merge = Gpu.post("merge", "Merge", 0, true);
        blit = Gpu.halo();
        grabUbo = Gpu.ubo("totemglow halo grab", 32);
        blurX = Gpu.ubo("totemglow halo x", 32);
        blurY = Gpu.ubo("totemglow halo y", 32);
        mergeUbo = Gpu.ubo("totemglow halo merge", 80);
    }

    private static final class Slot {
        final int size;
        final Gpu.Tex a;
        final Gpu.Tex b;
        final Gpu.Tex c;
        final Result result;
        long seen;
        long baked = -1L;

        Slot(int size, int pad) {
            this.size = size;
            this.a = Gpu.texture("totemglow halo a", size, size);
            this.b = Gpu.texture("totemglow halo b", size, size);
            this.c = Gpu.texture("totemglow halo c", size, size);
            this.result = new Result(this.c, pad);
        }

        void close() {
            this.a.close();
            this.b.close();
            this.c.close();
        }
    }

    public record Result(Gpu.Tex view, int pad) {
    }
}

