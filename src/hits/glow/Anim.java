/*
 * Decompiled with CFR 0.152.
 */
package hits.glow;

public enum Anim {
    STATIC("Static"),
    PULSE("Pulse"),
    RAINBOW("Rainbow"),
    FLICKER("Flicker"),
    SHIMMER("Shimmer");

    public final String label;

    private Anim(String label) {
        this.label = label;
    }
}

