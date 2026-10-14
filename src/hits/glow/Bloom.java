/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.ItemDisplayContext
 */
package hits.glow;

import hits.glow.Config;
import hits.glow.Glow;
import hits.glow.Gpu;
import hits.glow.Halo;
import hits.glow.Look;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemDisplayContext;

public final class Bloom {
    private static final float SPREAD = 6.0f;
    private static final int LEVELS = 4;
    private static final Set<List<?>> TAGGED = Collections.newSetFromMap(new IdentityHashMap());
    public static boolean pop;
    private static boolean open;
    private static boolean used;
    private static final Gpu.Tex[] down;
    private static Gpu.Tex spare;
    private static int width;
    private static int height;
    private static Gpu.Pipe shrink;
    private static Gpu.Pipe blur;
    private static Gpu.Pipe merger;
    private static Gpu.Buf[] passes;
    private static Gpu.Buf merge;

    private Bloom() {
    }

    public static void begin() {
        TAGGED.clear();
        used = false;
        Config c = Config.get();
        open = c.live() && (c.hand || c.world || c.pop) && Gpu.frame();
    }

    public static boolean want(ItemDisplayContext ctx) {
        if (!open || Gpu.redirected()) {
            return false;
        }
        Config c = Config.get();
        if (pop) {
            return c.pop;
        }
        return switch (ctx) {
            case ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND -> c.hand;
            case ItemDisplayContext.GUI, ItemDisplayContext.NONE -> false;
            default -> c.world;
        };
    }

    public static void tag(List<?> quads) {
        if (quads.isEmpty()) {
            return;
        }
        if (!used) {
            used = true;
            Gpu.clearMask();
        }
        TAGGED.add(quads);
    }

    public static boolean tagged(List<?> quads) {
        return !TAGGED.isEmpty() && TAGGED.contains(quads);
    }

    public static void end() {
        int levels;
        boolean run = open && used;
        open = false;
        used = false;
        TAGGED.clear();
        if (!run) {
            return;
        }
        Config c = Config.get();
        Look look = Look.of(c, Glow.seconds());
        if (look.dark()) {
            return;
        }
        Bloom.setup();
        Gpu.Tex screen = Gpu.screen();
        Gpu.Tex mask = Gpu.mask();
        int w = screen.width();
        int h = screen.height();
        float k = 6.0f * (float)h / 1080.0f;
        float sb = c.body.live() ? Math.max(0.05f, c.body.radius * k) : 0.05f;
        float se = c.eyes.live() ? Math.max(0.05f, c.eyes.radius * k) : 0.05f;
        float top = Math.max(sb, se);
        int n = levels = c.fast ? 2 : 1;
        while (levels < 4 && top / (float)(1 << levels) > 12.0f) {
            ++levels;
        }
        Bloom.size(w, h, levels);
        Gpu.Tex src = mask;
        int sw = w;
        int sh = h;
        for (int i = 0; i < levels; ++i) {
            Gpu.write(passes[i], 1.0f / (float)sw, 1.0f / (float)sh, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
            Gpu.draw(shrink, down[i], "Pass", passes[i], src, null);
            src = down[i];
            sw = Math.max(1, sw >> 1);
            sh = Math.max(1, sh >> 1);
        }
        float d = 1 << levels;
        float bs = sb / d;
        float es = se / d;
        float taps = (float)Math.min(96.0, Math.ceil(Math.max(bs, es) * 3.0f));
        Gpu.Tex last = down[levels - 1];
        Gpu.write(passes[4], 1.0f / (float)sw, 0.0f, 0.0f, 0.0f, bs, es, taps, 0.0f);
        Gpu.draw(blur, spare, "Pass", passes[4], last, null);
        Gpu.write(passes[5], 0.0f, 1.0f / (float)sh, 0.0f, 0.0f, bs, es, taps, 0.0f);
        Gpu.draw(blur, last, "Pass", passes[5], spare, null);
        Halo.merge(merge, look, w, h, 9.0f);
        Gpu.draw(merger, screen, "Merge", merge, last, mask);
    }

    private static void setup() {
        if (shrink != null) {
            return;
        }
        shrink = Gpu.post("down", "Pass", 0, false);
        blur = Gpu.post("blur", "Pass", 0, false);
        merger = Gpu.post("merge", "Merge", 1, true);
        passes = new Gpu.Buf[6];
        for (int i = 0; i < passes.length; ++i) {
            Bloom.passes[i] = Gpu.ubo("totemglow pass " + i, 32);
        }
        merge = Gpu.ubo("totemglow merge", 80);
    }

    private static void size(int w, int h, int levels) {
        int i;
        if (w != width || h != height) {
            for (i = 0; i < 4; ++i) {
                if (down[i] == null) continue;
                down[i].close();
                Bloom.down[i] = null;
            }
            if (spare != null) {
                spare.close();
                spare = null;
            }
            width = w;
            height = h;
        }
        for (i = 0; i < levels; ++i) {
            if (down[i] != null) continue;
            Bloom.down[i] = Gpu.texture("totemglow down " + i, Math.max(1, w >> i + 1), Math.max(1, h >> i + 1));
        }
        int lw = Math.max(1, w >> levels);
        int lh = Math.max(1, h >> levels);
        if (spare == null || spare.width() != lw || spare.height() != lh) {
            if (spare != null) {
                spare.close();
            }
            spare = Gpu.texture("totemglow blur", lw, lh);
        }
    }

    static {
        down = new Gpu.Tex[4];
    }
}

