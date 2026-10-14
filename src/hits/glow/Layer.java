/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

import hits.glow.Colors;

public final class Layer {
    public boolean on = true;
    public int color;
    public float radius;
    public float strength;
    public float falloff;

    public Layer() {
    }

    public Layer(int color, float radius, float strength, float falloff) {
        this.color = color;
        this.radius = radius;
        this.strength = strength;
        this.falloff = falloff;
    }

    public Layer(int color, float radius, float strength, float falloff, boolean on) {
        this(color, radius, strength, falloff);
        this.on = on;
    }

    public Layer copy() {
        Layer l = new Layer(this.color, this.radius, this.strength, this.falloff);
        l.on = this.on;
        return l;
    }

    void fix() {
        this.color &= 0xFFFFFF;
        this.radius = Colors.clamp(this.radius, 0.0f, 10.0f);
        this.strength = Colors.clamp(this.strength, 0.0f, 5.0f);
        this.falloff = Colors.clamp(this.falloff, 0.25f, 3.0f);
        if (Float.isNaN(this.radius)) {
            this.radius = 3.0f;
        }
        if (Float.isNaN(this.strength)) {
            this.strength = 1.0f;
        }
        if (Float.isNaN(this.falloff)) {
            this.falloff = 1.0f;
        }
    }

    public boolean live() {
        return this.on && this.strength > 0.001f;
    }
}

