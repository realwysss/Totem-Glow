/*
 * Decompiled with CFR 0.152.
 */
package hits.glow.ui;

import hits.glow.Anim;
import hits.glow.Colors;
import hits.glow.Config;
import hits.glow.Layer;
import hits.glow.ui.Canvas;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class Menu {
    public static final int BACK = 1;
    public static final int ENTER = 2;
    public static final int ESCAPE = 3;
    public static final int TAB = 4;
    public static final int PASTE = 5;
    public static final int COPY = 6;
    private static final int TEXT = -789256;
    private static final int MUTED = -6577746;
    private static final int WELL = 0x42000000;
    private static final int EDGE = 0x21FFFFFF;
    private static final int[] BACKDROPS = new int[]{-15987438, -13743514, -11176897, -2567484};
    private static final String[] BACKDROP_NAMES = new String[]{"Night", "Sky", "Grass", "Sand"};
    private static final String[] TABS = new String[]{"Eyes", "Body", "Effects", "Show"};
    private final Config start;
    private final Runnable close;
    private final Supplier<String> clipboard;
    private final List<Hit> hits = new ArrayList<Hit>();
    private final Map<String, Float> fx = new HashMap<String, Float>();
    private final float[] eyeHsv;
    private final float[] bodyHsv;
    private String active;
    private Drag drag;
    private int tab;
    private int backdrop;
    private boolean typing;
    private String hex = "";
    private float open;
    private long last;
    private float dt;
    private float clock;
    private double mx;
    private double my;
    private String tip;

    public Menu(Runnable close, Supplier<String> clipboard) {
        this.close = close;
        this.clipboard = clipboard;
        this.start = Config.get().copy();
        this.eyeHsv = Colors.hsv(Config.get().eyes.color);
        this.bodyHsv = Colors.hsv(Config.get().body.color);
    }

    public void cancel() {
        Config.set(this.start.copy());
    }

    public void save() {
        this.commitHex();
        Config.get().fix();
        Config.save();
    }

    public void draw(Canvas g, int width, int height, double mouseX, double mouseY) {
        long now = System.nanoTime();
        this.dt = this.last == 0L ? 0.0f : Math.min((float)(now - this.last) / 1.0E9f, 0.1f);
        this.last = now;
        this.clock += this.dt;
        this.mx = mouseX;
        this.my = mouseY;
        this.hits.clear();
        this.tip = null;
        this.open = this.ease(this.open, 1.0f, 9.0f);
        Config c = Config.get();
        float cw = Math.min(width - 24, 430);
        float ch = Math.min(height - 12, 252);
        float x = ((float)width - cw) / 2.0f;
        float y = ((float)height - ch) / 2.0f;
        int acc = this.accent();
        g.fill(0.0f, 0.0f, width, height, 0.0f, this.fade(0x22000000), this.fade(0x7A000000));
        g.shadow(x + cw * 0.18f, y + ch * 0.12f, cw * 0.64f, ch * 0.64f, 60.0f, 70.0f, this.fade(Colors.alpha(acc, 0.13f)));
        g.shadow(x, y + 10.0f, cw, ch, 16.0f, 28.0f, this.fade(-1728053248));
        g.fill(x, y, cw, ch, 14.0f, this.fade(-1373231053), this.fade(-1039200235));
        g.ring(x, y, cw, ch, 14.0f, 1.0f, this.fade(0x5CFFFFFF), this.fade(0xFFFFFFF));
        this.header(g, c, x, y, cw);
        g.fill(x + 16.0f, y + 32.0f, cw - 32.0f, 1.0f, 0.0f, this.fade(0x16FFFFFF));
        float top = y + 42.0f;
        float bottom = y + ch - 36.0f;
        float lw = Math.max(108.0f, Math.min(146.0f, cw * 0.34f));
        float lx = x + 14.0f;
        this.preview(g, lx, top, lw, bottom - top);
        float rx = lx + lw + 12.0f;
        float rw = x + cw - 14.0f - rx;
        this.segmented(g, "tabs", rx, top, rw, 18.0f, TABS, this.tab, i -> {
            this.commitHex();
            this.tab = i;
        });
        float cy = top + 26.0f;
        float space = bottom - cy;
        switch (this.tab) {
            case 0: {
                this.layer(g, "e", "Eye glow", c.eyes, this.eyeHsv, rx, cy, rw, space);
                break;
            }
            case 1: {
                this.layer(g, "b", "Body glow", c.body, this.bodyHsv, rx, cy, rw, space);
                break;
            }
            case 2: {
                this.effects(g, c, rx, cy, rw);
                break;
            }
            default: {
                this.show(g, c, rx, cy, rw);
            }
        }
        this.footer(g, x, y + ch - 28.0f, cw);
        if (this.tip != null) {
            float tw = g.width(this.tip) + 12;
            float tx = (float)Math.min(this.mx + 10.0, (double)((float)width - tw - 4.0f));
            float ty = (float)Math.max(this.my - 18.0, 2.0);
            g.shadow(tx, ty, tw, 15.0f, 5.0f, 6.0f, this.fade(-1879048192));
            g.fill(tx, ty, tw, 15.0f, 5.0f, this.fade(-299226058), this.fade(-300213213));
            g.ring(tx, ty, tw, 15.0f, 5.0f, 1.0f, this.fade(0x33FFFFFF), this.fade(0xFFFFFFF));
            g.text(this.tip, tx + 6.0f, ty + 4.0f, this.fade(-789256));
        }
    }

    private void header(Canvas g, Config c, float x, float y, float w) {
        g.text("Totem Glow", x + 16.0f, y + 12.0f, this.fade(-789256));
        String state = c.enabled ? "On" : "Off";
        float tx = x + w - 38.0f;
        float lx = tx - 6.0f - (float)g.width(state);
        Runnable flip = () -> {
            c.enabled = !c.enabled;
        };
        g.text(state, lx, y + 12.0f, this.fade(c.enabled ? -789256 : -6577746));
        this.toggle(g, "master", tx, y + 10.0f, c.enabled, flip);
        this.hits.add(new Hit("masterrow", lx - 4.0f, y + 6.0f, tx + 26.0f - (lx - 4.0f), 18.0f, flip, null, null));
    }

    private void preview(Canvas g, float x, float y, float w, float h) {
        g.fill(x, y, w, h, 10.0f, this.fade(0x30000000), this.fade(0x58000000));
        float bgT = this.anim("bg", this.backdrop, 10.0f);
        int from = BACKDROPS[(int)Math.floor(bgT) % BACKDROPS.length];
        int to = BACKDROPS[(int)Math.ceil(bgT) % BACKDROPS.length];
        int bg = Colors.mix(from, to, bgT - (float)Math.floor(bgT));
        g.fill(x + 3.0f, y + 3.0f, w - 6.0f, h - 6.0f, 8.0f, this.fade(Colors.alpha(bg, 0.9f)), this.fade(Colors.mix(bg, -16777216, 0.35f)));
        g.ring(x, y, w, h, 10.0f, 1.0f, this.fade(0x38FFFFFF), this.fade(0xAFFFFFF));
        int label = Colors.luma(bg) > 0.55f ? -1442840576 : -1711276033;
        g.text("Preview", x + 10.0f, y + 9.0f, this.fade(label));
        float size = Math.max(32.0f, Math.min(w - 28.0f, h - 50.0f) * 0.82f);
        size = (float)Math.round(size / 16.0f) * 16.0f;
        if (size > Math.min(w - 20.0f, h - 44.0f)) {
            size -= 16.0f;
        }
        float bob = (float)Math.sin(this.clock * 1.7f) * 2.5f;
        g.totem(x + (w - size) / 2.0f, y + (h - size) / 2.0f - 3.0f + bob, size);
        float d = 8.0f;
        float gap = 7.0f;
        float total = (float)BACKDROPS.length * d + (float)(BACKDROPS.length - 1) * gap;
        float dx = x + (w - total) / 2.0f;
        float dy = y + h - 17.0f;
        for (int i = 0; i < BACKDROPS.length; ++i) {
            float px = dx + (float)i * (d + gap);
            boolean over = this.hover(px - 3.0f, dy - 3.0f, d + 6.0f, d + 6.0f);
            float k = this.anim("dot" + i, over ? 1.0f : 0.0f, 16.0f);
            float s = d + k * 2.0f;
            g.fill(px - k, dy - k, s, s, s / 2.0f, this.fade(BACKDROPS[i]));
            g.ring(px - k - 1.5f, dy - k - 1.5f, s + 3.0f, s + 3.0f, (s + 3.0f) / 2.0f, 1.0f, this.fade(i == this.backdrop ? -570425345 : 0x40FFFFFF));
            if (over) {
                this.tip = BACKDROP_NAMES[i];
            }
            int idx = i;
            this.hits.add(new Hit("dot" + i, px - 3.0f, dy - 3.0f, d + 6.0f, d + 6.0f, () -> {
                this.backdrop = idx;
            }, null, null));
        }
    }

    private void layer(Canvas g, String id, String name, Layer l, float[] hsv, float x, float y, float w, float space) {
        this.row(g, id + "on", name, null, x, y, w, l.on, () -> {
            l.on = !l.on;
        });
        float step = space >= 140.0f ? 24.0f : 22.0f;
        float ph = Math.max(34.0f, Math.min(56.0f, space - 43.0f - step * 2.0f));
        float py = y + 22.0f;
        this.picker(g, id, l, hsv, x, py, w, ph);
        float sy = py + ph + 8.0f;
        this.slider(g, id + "r", "Radius", x, sy, w, l.radius, 0.0f, 10.0f, 0.1f, v -> {
            l.radius = (float)v;
        }, Menu.fmt1(l.radius));
        this.slider(g, id + "s", "Strength", x, sy + step, w, l.strength, 0.0f, 5.0f, 0.05f, v -> {
            l.strength = (float)v;
        }, Menu.fmt2(l.strength));
        this.slider(g, id + "f", "Falloff", x, sy + step * 2.0f, w, l.falloff, 0.25f, 3.0f, 0.05f, v -> {
            l.falloff = (float)v;
        }, Menu.fmt2(l.falloff));
    }

    private void effects(Canvas g, Config c, float x, float y, float w) {
        g.text("Animation", x, y + 1.0f, this.fade(-6577746));
        Anim[] all = Anim.values();
        String[] names = new String[all.length];
        for (int i2 = 0; i2 < all.length; ++i2) {
            names[i2] = all[i2].label;
        }
        this.segmented(g, "anim", x, y + 12.0f, w, 16.0f, names, c.anim.ordinal(), i -> {
            c.anim = all[i];
        });
        this.slider(g, "speed", "Speed", x, y + 36.0f, w, c.speed, 0.1f, 4.0f, 0.05f, v -> {
            c.speed = (float)v;
        }, Menu.fmt1(c.speed) + "x");
        this.slider(g, "inner", "Glow over totem", x, y + 58.0f, w, c.inner, 0.0f, 1.0f, 0.01f, v -> {
            c.inner = (float)v;
        }, Math.round(c.inner * 100.0f) + "%");
        float sw = Math.min(130.0f, w * 0.52f);
        g.text("Blend", x, y + 88.0f, this.fade(-6577746));
        this.segmented(g, "blend", x + w - sw, y + 84.0f, sw, 16.0f, new String[]{"Additive", "Screen"}, c.screen ? 1 : 0, i -> {
            c.screen = i == 1;
        });
        g.text("Quality", x, y + 110.0f, this.fade(-6577746));
        this.segmented(g, "fast", x + w - sw, y + 106.0f, sw, 16.0f, new String[]{"High", "Fast"}, c.fast ? 1 : 0, i -> {
            c.fast = i == 1;
        });
    }

    private void show(Canvas g, Config c, float x, float y, float w) {
        this.row(g, "hand", "First person", "In your hands", x, y, w, c.hand, () -> {
            c.hand = !c.hand;
        });
        this.row(g, "gui", "Hotbar and inventory", "Item slots in menus", x, y + 30.0f, w, c.gui, () -> {
            c.gui = !c.gui;
        });
        this.row(g, "world", "World", "Dropped, framed, held by mobs", x, y + 60.0f, w, c.world, () -> {
            c.world = !c.world;
        });
        this.row(g, "pop", "Totem pop", "When it saves you", x, y + 90.0f, w, c.pop, () -> {
            c.pop = !c.pop;
        });
    }

    private void footer(Canvas g, float x, float y, float w) {
        float bw = 60.0f;
        float right = x + w - 14.0f;
        this.button(g, "done", "Done", right - bw, y, bw, true, () -> {
            this.save();
            this.close.run();
        });
        this.button(g, "cancel", "Cancel", right - bw * 2.0f - 8.0f, y, bw, false, () -> {
            this.cancel();
            this.close.run();
        });
        this.button(g, "reset", "Defaults", x + 14.0f, y, bw + 6.0f, false, () -> {
            Config.set(new Config());
            this.sync();
        });
    }

    private void row(Canvas g, String id, String label, String note, float x, float y, float w, boolean on, Runnable flip) {
        float h = note == null ? 16.0f : 26.0f;
        boolean over = this.hover(x - 4.0f, y - 3.0f, w + 8.0f, h);
        float k = this.anim(id + "row", over ? 1.0f : 0.0f, 14.0f);
        if (k > 0.01f) {
            g.fill(x - 4.0f, y - 3.0f, w + 8.0f, h, 7.0f, this.fade(Colors.alpha(0x16FFFFFF, k)), this.fade(Colors.alpha(0xAFFFFFF, k)));
        }
        if (note == null) {
            g.text(label, x + 2.0f, y + 1.0f, this.fade(on ? -789256 : -6577746));
            this.toggle(g, id, x + w - 24.0f, y - 1.0f, on, flip);
        } else {
            g.text(label, x + 2.0f, y + 1.0f, this.fade(-789256));
            g.text(note, x + 2.0f, y + 12.0f, this.fade(-6577746));
            this.toggle(g, id, x + w - 24.0f, y + 5.0f, on, flip);
        }
        this.hits.add(new Hit(id + "row", x - 4.0f, y - 3.0f, w + 8.0f, h, flip, null, null));
    }

    private void toggle(Canvas g, String id, float x, float y, boolean on, Runnable flip) {
        float w = 22.0f;
        float h = 12.0f;
        float t = this.anim(id, on ? 1.0f : 0.0f, 14.0f);
        boolean over = this.hover(x - 2.0f, y - 2.0f, w + 4.0f, h + 4.0f);
        float hl = this.anim(id + "h", over ? 1.0f : 0.0f, 14.0f);
        int acc = this.accent();
        int topCol = Colors.mix(0x30FFFFFF, Colors.mix(acc, -1, 0.18f), t);
        int bottomCol = Colors.mix(0x42000000, acc, t);
        g.fill(x, y, w, h, h / 2.0f, this.fade(Colors.mix(topCol, -1, hl * 0.08f)), this.fade(bottomCol));
        g.ring(x, y, w, h, h / 2.0f, 1.0f, this.fade(0x33FFFFFF), this.fade(0xDFFFFFF));
        float k = 8.0f + hl;
        float kx = x + 2.0f + (w - 4.0f - k) * t;
        float ky = y + (h - k) / 2.0f;
        g.shadow(kx, ky + 0.5f, k, k, k / 2.0f, 2.0f, this.fade(0x70000000));
        g.fill(kx, ky, k, k, k / 2.0f, this.fade(-1), this.fade(-1775634));
        this.hits.add(new Hit(id, x - 2.0f, y - 2.0f, w + 4.0f, h + 4.0f, flip, null, null));
    }

    private void slider(Canvas g, String id, String label, float x, float y, float w, float v, float lo, float hi, float step, DoubleConsumer set, String shown) {
        g.text(label, x, y, this.fade(-6577746));
        g.text(shown, x + w - (float)g.width(shown), y, this.fade(-789256));
        float ty = y + 11.0f;
        float th = 4.0f;
        float t = Colors.clamp((v - lo) / (hi - lo), 0.0f, 1.0f);
        boolean held = id.equals(this.active);
        float k = this.anim(id, held || this.hover(x - 4.0f, ty - 6.0f, w + 8.0f, th + 12.0f) ? 1.0f : 0.0f, 16.0f);
        float at = this.anim(id + "v", t, 22.0f);
        int acc = this.accent();
        g.fill(x, ty, w, th, th / 2.0f, this.fade(0x42000000));
        g.fill(x, ty, Math.max(th, w * at), th, th / 2.0f, this.fade(Colors.mix(acc, -1, 0.25f)), this.fade(acc));
        float s = 8.0f + k * 2.0f;
        float kx = x + w * at - s / 2.0f;
        float ky = ty + th / 2.0f - s / 2.0f;
        if (k > 0.01f) {
            g.fill(kx - 3.0f * k, ky - 3.0f * k, s + 6.0f * k, s + 6.0f * k, (s + 6.0f * k) / 2.0f, this.fade(Colors.alpha(acc & 0xFFFFFF | 0x40000000, k)));
        }
        g.shadow(kx, ky + 0.5f, s, s, s / 2.0f, 2.5f, this.fade(Integer.MIN_VALUE));
        g.fill(kx, ky, s, s, s / 2.0f, this.fade(-1), this.fade(-1775634));
        Drag d = (px, py) -> set.accept(Menu.snap(lo + (hi - lo) * Colors.clamp((float)((px - (double)x) / (double)w), 0.0f, 1.0f), step, lo, hi));
        this.hits.add(new Hit(id, x - 4.0f, ty - 7.0f, w + 8.0f, th + 14.0f, null, d, a -> set.accept(Menu.snap(v + (float)Math.signum(a) * step * 5.0f, step, lo, hi))));
    }

    private void segmented(Canvas g, String id, float x, float y, float w, float h, String[] items, int sel, IntConsumer pick) {
        g.fill(x, y, w, h, h / 2.0f, this.fade(0x42000000));
        g.ring(x, y, w, h, h / 2.0f, 1.0f, this.fade(0x21FFFFFF), this.fade(0xAFFFFFF));
        int n = items.length;
        float[] edge = new float[n + 1];
        float sum = 0.0f;
        for (String s : items) {
            sum += (float)g.width(s);
        }
        float pad = (w - sum) / (float)n;
        edge[0] = x;
        for (int i = 0; i < n; ++i) {
            edge[i + 1] = edge[i] + (float)g.width(items[i]) + pad;
        }
        edge[n] = x + w;
        float px = this.anim(id, edge[sel], 16.0f);
        float pw = this.anim(id + "w", edge[sel + 1] - edge[sel], 16.0f);
        int acc = this.accent();
        g.shadow(px + 2.0f, y + 2.0f, pw - 4.0f, h - 4.0f, (h - 4.0f) / 2.0f, 3.0f, this.fade(0x60000000));
        g.fill(px + 2.0f, y + 2.0f, pw - 4.0f, h - 4.0f, (h - 4.0f) / 2.0f, this.fade(0x36FFFFFF), this.fade(0x1CFFFFFF));
        g.ring(px + 2.0f, y + 2.0f, pw - 4.0f, h - 4.0f, (h - 4.0f) / 2.0f, 1.0f, this.fade(Colors.alpha(acc, 0.75f)), this.fade(Colors.alpha(acc, 0.25f)));
        for (int i = 0; i < n; ++i) {
            float ix = edge[i];
            float iw = edge[i + 1] - ix;
            boolean over = this.hover(ix, y, iw, h);
            float k = this.anim(id + i, over ? 1.0f : 0.0f, 14.0f);
            int col = i == sel ? -789256 : Colors.mix(-6577746, -789256, k * 0.6f);
            g.text(items[i], ix + (iw - (float)g.width(items[i])) / 2.0f, y + (h - 8.0f) / 2.0f, this.fade(col));
            int idx = i;
            this.hits.add(new Hit(id + i, ix, y, iw, h, () -> pick.accept(idx), null, a -> pick.accept(Math.floorMod(sel - (int)Math.signum(a), n))));
        }
    }

    private void button(Canvas g, String id, String label, float x, float y, float w, boolean main, Runnable run) {
        float h = 18.0f;
        boolean over = this.hover(x, y, w, h);
        float k = this.anim(id, over ? 1.0f : 0.0f, 14.0f);
        int acc = this.accent();
        g.shadow(x, y + 1.0f, w, h, 7.0f, 5.0f, this.fade(0x70000000));
        if (main) {
            g.fill(x, y, w, h, 7.0f, this.fade(Colors.mix(Colors.mix(acc, -1, 0.22f), -1, k * 0.12f)), this.fade(Colors.mix(acc, -1, k * 0.08f)));
            g.ring(x, y, w, h, 7.0f, 1.0f, this.fade(0x55FFFFFF), this.fade(0x14FFFFFF));
        } else {
            g.fill(x, y, w, h, 7.0f, this.fade(Colors.mix(0x26FFFFFF, 0x3DFFFFFF, k)), this.fade(Colors.mix(0x12FFFFFF, 0x22FFFFFF, k)));
            g.ring(x, y, w, h, 7.0f, 1.0f, this.fade(0x2EFFFFFF), this.fade(0xAFFFFFF));
        }
        int tc = main && Colors.luma(acc) > 0.62f ? -15395301 : -789256;
        g.text(label, x + (w - (float)g.width(label)) / 2.0f, y + 5.0f, this.fade(tc));
        this.hits.add(new Hit(id, x, y, w, h, run, null, null));
    }

    private void picker(Canvas g, String id, Layer l, float[] hsv, float x, float y, float w, float h) {
        float sw = Math.round(w * 0.52f);
        int hueCol = 0xFF000000 | Colors.fromHsv(hsv[0], 1.0f, 1.0f);
        g.shadow(x, y + 1.0f, sw, h, 7.0f, 5.0f, this.fade(0x70000000));
        g.sv(x, y, sw, h, 7.0f, this.fade(hueCol));
        g.ring(x, y, sw, h, 7.0f, 1.0f, this.fade(0x33FFFFFF), this.fade(0x14FFFFFF));
        float mxp = x + hsv[1] * sw;
        float myp = y + (1.0f - hsv[2]) * h;
        g.shadow(mxp - 4.5f, myp - 4.5f, 9.0f, 9.0f, 4.5f, 2.0f, this.fade(-1879048192));
        g.fill(mxp - 4.5f, myp - 4.5f, 9.0f, 9.0f, 4.5f, this.fade(-1));
        g.fill(mxp - 3.0f, myp - 3.0f, 6.0f, 6.0f, 3.0f, this.fade(0xFF000000 | l.color));
        this.hits.add(new Hit(id + "sv", x, y, sw, h, null, (px, py) -> {
            hsv[1] = Colors.clamp((float)((px - (double)x) / (double)sw), 0.0f, 1.0f);
            hsv[2] = 1.0f - Colors.clamp((float)((py - (double)y) / (double)h), 0.0f, 1.0f);
            l.color = Colors.fromHsv(hsv[0], hsv[1], hsv[2]);
            this.typing = false;
        }, null));
        float rx = x + sw + 10.0f;
        float rw = w - sw - 10.0f;
        g.shadow(rx, y + 1.0f, rw, 12.0f, 6.0f, 3.0f, this.fade(0x60000000));
        g.hue(rx, y, rw, 12.0f, 6.0f, this.fade(-1));
        g.ring(rx, y, rw, 12.0f, 6.0f, 1.0f, this.fade(0x33FFFFFF), this.fade(0x14FFFFFF));
        float hx = rx + hsv[0] * rw;
        g.shadow(hx - 3.5f, y - 2.0f, 7.0f, 16.0f, 3.5f, 2.0f, this.fade(-1879048192));
        g.fill(hx - 3.5f, y - 2.0f, 7.0f, 16.0f, 3.5f, this.fade(-1), this.fade(-1775634));
        g.fill(hx - 2.0f, y + 0.5f, 4.0f, 11.0f, 2.0f, this.fade(hueCol));
        this.hits.add(new Hit(id + "hue", rx - 3.0f, y - 3.0f, rw + 6.0f, 18.0f, null, (px, py) -> {
            hsv[0] = Colors.clamp((float)((px - (double)rx) / (double)rw), 0.0f, 0.9999f);
            l.color = Colors.fromHsv(hsv[0], hsv[1], hsv[2]);
            this.typing = false;
        }, a -> {
            hsv[0] = (hsv[0] + (float)Math.signum(a) * 0.01f + 1.0f) % 1.0f;
            l.color = Colors.fromHsv(hsv[0], hsv[1], hsv[2]);
        }));
        float fy = y + 20.0f;
        float sq = 18.0f;
        float fw = rw - sq - 6.0f;
        boolean focus = this.typing && this.active == null && this.tabLayer() == l;
        float k = this.anim(id + "hex", focus ? 1.0f : (this.hover(rx, fy, fw, sq) ? 0.4f : 0.0f), 14.0f);
        g.fill(rx, fy, fw, sq, 7.0f, this.fade(0x42000000), this.fade(0x30000000));
        g.ring(rx, fy, fw, sq, 7.0f, 1.0f, this.fade(Colors.mix(0x21FFFFFF, this.accent(), k)), this.fade(Colors.mix(0xAFFFFFF, this.accent(), k * 0.5f)));
        Object shown = focus ? "#" + this.hex : Colors.hex(l.color);
        g.text((String)shown, rx + 7.0f, fy + 5.0f, this.fade(focus && !Menu.valid(this.hex) ? -32640 : -789256));
        if (focus && this.clock % 1.0f < 0.55f) {
            float cx = rx + 7.0f + (float)g.width((String)shown) + 1.0f;
            g.fill(cx, fy + 4.0f, 1.0f, 10.0f, 0.0f, this.fade(-789256));
        }
        this.hits.add(new Hit(id + "hexf", rx, fy, fw, sq, () -> {
            this.typing = true;
            this.hex = Colors.hex(l.color).substring(1);
        }, null, null));
        float sx = rx + rw - sq;
        g.shadow(sx, fy + 1.0f, sq, sq, 7.0f, 4.0f, this.fade(Colors.alpha(0xFF000000 | l.color, 0.45f)));
        g.fill(sx, fy, sq, sq, 7.0f, this.fade(0xFF000000 | Colors.mix(0xFF000000 | l.color, -1, 0.18f)), this.fade(0xFF000000 | l.color));
        g.ring(sx, fy, sq, sq, 7.0f, 1.0f, this.fade(0x55FFFFFF), this.fade(0x14FFFFFF));
    }

    private Layer tabLayer() {
        return this.tab == 0 ? Config.get().eyes : (this.tab == 1 ? Config.get().body : null);
    }

    private float[] tabHsv() {
        return this.tab == 0 ? this.eyeHsv : (float[])(this.tab == 1 ? this.bodyHsv : null);
    }

    private void sync() {
        System.arraycopy(Colors.hsv(Config.get().eyes.color), 0, this.eyeHsv, 0, 3);
        System.arraycopy(Colors.hsv(Config.get().body.color), 0, this.bodyHsv, 0, 3);
    }

    private void commitHex() {
        if (!this.typing) {
            return;
        }
        this.typing = false;
        Layer l = this.tabLayer();
        if (l != null && Menu.valid(this.hex)) {
            this.apply(l, this.hex);
        }
    }

    private void apply(Layer l, String s) {
        int col;
        l.color = col = Colors.parse(s, l.color);
        float[] hsv = this.tabHsv();
        if (hsv != null) {
            System.arraycopy(Colors.hsv(col), 0, hsv, 0, 3);
        }
    }

    private static boolean valid(String s) {
        return (s.length() == 6 || s.length() == 3) && s.chars().allMatch(ch -> Character.digit(ch, 16) >= 0);
    }

    public boolean click(double x, double y, int button) {
        this.mx = x;
        this.my = y;
        if (button != 0) {
            return false;
        }
        for (int i = this.hits.size() - 1; i >= 0; --i) {
            Hit h = this.hits.get(i);
            if (!h.in(x, y)) continue;
            if (!h.id.endsWith("hexf")) {
                this.commitHex();
            }
            if (h.drag != null) {
                this.active = h.id;
                this.drag = h.drag;
                this.drag.to(x, y);
            }
            if (h.click != null) {
                h.click.run();
            }
            return true;
        }
        this.commitHex();
        return false;
    }

    public boolean release(double x, double y, int button) {
        boolean was = this.drag != null;
        this.drag = null;
        this.active = null;
        return was;
    }

    public boolean dragged(double x, double y) {
        this.mx = x;
        this.my = y;
        if (this.drag == null) {
            return false;
        }
        this.drag.to(x, y);
        return true;
    }

    public boolean scroll(double x, double y, double amount) {
        if (amount == 0.0) {
            return false;
        }
        for (int i = this.hits.size() - 1; i >= 0; --i) {
            Hit h = this.hits.get(i);
            if (h.scroll == null || !h.in(x, y)) continue;
            h.scroll.accept(amount);
            return true;
        }
        return false;
    }

    public boolean key(int action) {
        if (this.typing) {
            Layer l = this.tabLayer();
            switch (action) {
                case 1: {
                    if (!this.hex.isEmpty()) {
                        this.hex = this.hex.substring(0, this.hex.length() - 1);
                    }
                    return true;
                }
                case 2: {
                    this.commitHex();
                    return true;
                }
                case 3: {
                    this.typing = false;
                    return true;
                }
                case 5: {
                    String t;
                    String s = this.clipboard.get();
                    if (s != null && l != null && Menu.valid(t = s.trim().replace("#", ""))) {
                        this.hex = t.toUpperCase(Locale.ROOT);
                        this.apply(l, this.hex);
                    }
                    return true;
                }
                case 6: {
                    return true;
                }
            }
            return false;
        }
        if (action == 4) {
            this.tab = (this.tab + 1) % TABS.length;
            return true;
        }
        return false;
    }

    public boolean type(int codepoint) {
        if (!this.typing) {
            return false;
        }
        if (Character.digit(codepoint, 16) < 0) {
            return true;
        }
        if (this.hex.length() < 6) {
            this.hex = this.hex + Character.toUpperCase((char)codepoint);
            Layer l = this.tabLayer();
            if (l != null && this.hex.length() == 6) {
                this.apply(l, this.hex);
            }
        }
        return true;
    }

    public boolean typing() {
        return this.typing;
    }

    private int accent() {
        Config c = Config.get();
        int b = this.tab == 1 || !c.eyes.on ? c.body.color : c.eyes.color;
        int col = 0xFF000000 | b;
        return Colors.luma(b) < 0.22f ? 0xFF000000 | Colors.mix(col, -1, 0.4f) : col;
    }

    private boolean hover(float x, float y, float w, float h) {
        return this.drag == null && this.mx >= (double)x && this.my >= (double)y && this.mx < (double)(x + w) && this.my < (double)(y + h);
    }

    private float anim(String key, float target, float speed) {
        Float cur = this.fx.get(key);
        float v = cur == null ? target : this.ease(cur.floatValue(), target, speed);
        this.fx.put(key, Float.valueOf(v));
        return v;
    }

    private float ease(float cur, float to, float speed) {
        float v = cur + (to - cur) * (1.0f - (float)Math.exp(-speed * this.dt));
        return Math.abs(to - v) < 5.0E-4f ? to : v;
    }

    private int fade(int argb) {
        return Colors.alpha(argb, this.open);
    }

    private static float snap(float v, float step, float lo, float hi) {
        float s = (float)Math.round(v / step) * step;
        return Colors.clamp(s, lo, hi);
    }

    private static String fmt1(float v) {
        return String.format(Locale.ROOT, "%.1f", Float.valueOf(v));
    }

    private static String fmt2(float v) {
        return String.format(Locale.ROOT, "%.2f", Float.valueOf(v));
    }

    private record Hit(String id, float x, float y, float w, float h, Runnable click, Drag drag, DoubleConsumer scroll) {
        boolean in(double px, double py) {
            return px >= (double)this.x && py >= (double)this.y && px < (double)(this.x + this.w) && py < (double)(this.y + this.h);
        }
    }

    public static interface Drag {
        public void to(double var1, double var3);
    }
}

