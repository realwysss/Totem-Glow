/*
 * Decompiled with CFR 0.152.
 */
package hits.glow.ui;

public interface Canvas {
    public void fill(float var1, float var2, float var3, float var4, float var5, int var6, int var7);

    public void ring(float var1, float var2, float var3, float var4, float var5, float var6, int var7, int var8);

    public void shadow(float var1, float var2, float var3, float var4, float var5, float var6, int var7);

    public void hue(float var1, float var2, float var3, float var4, float var5, int var6);

    public void sv(float var1, float var2, float var3, float var4, float var5, int var6);

    public void text(String var1, float var2, float var3, int var4);

    public int width(String var1);

    public void totem(float var1, float var2, float var3);

    default public void fill(float x, float y, float w, float h, float r, int argb) {
        this.fill(x, y, w, h, r, argb, argb);
    }

    default public void ring(float x, float y, float w, float h, float r, float t, int argb) {
        this.ring(x, y, w, h, r, t, argb, argb);
    }
}

