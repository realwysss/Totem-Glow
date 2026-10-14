/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.gui.GuiGraphicsExtractor
 *  net.minecraft.client.gui.navigation.ScreenRectangle
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.input.CharacterEvent
 *  net.minecraft.client.input.KeyEvent
 *  net.minecraft.client.input.MouseButtonEvent
 *  net.minecraft.client.renderer.RenderPipelines
 *  net.minecraft.client.renderer.state.gui.GuiElementRenderState
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureAtlasSprite
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.Identifier
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 */
package hits.glow.ui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hits.glow.Frost;
import hits.glow.Gpu;
import hits.glow.Halo;
import hits.glow.ui.Canvas;
import hits.glow.ui.Menu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

public final class Panel
extends Screen
implements Canvas {
    private static final Identifier TOTEM = Identifier.withDefaultNamespace((String)"item/totem_of_undying");
    private final Screen parent;
    private final Menu menu;
    private GuiGraphicsExtractor g;

    public Panel(Screen parent) {
        super((Component)Component.literal((String)"Totem Glow"));
        this.parent = parent;
        this.menu = new Menu(this::back, () -> this.minecraft == null ? null : this.minecraft.keyboardHandler.getClipboard());
    }

    private void back() {
        this.minecraft.gui.setScreen(this.parent);
    }

    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        if (this.minecraft.level == null) {
            this.extractPanorama(g, delta);
        }
        Frost.request();
        g.blurBeforeThisStratum();
    }

    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        this.g = g;
        this.menu.draw(this, this.width, this.height, mouseX, mouseY);
    }

    public boolean mouseClicked(MouseButtonEvent e, boolean twice) {
        return this.menu.click(e.x(), e.y(), e.button()) || super.mouseClicked(e, twice);
    }

    public boolean mouseReleased(MouseButtonEvent e) {
        return this.menu.release(e.x(), e.y(), e.button()) || super.mouseReleased(e);
    }

    public boolean mouseDragged(MouseButtonEvent e, double dx, double dy) {
        return this.menu.dragged(e.x(), e.y()) || super.mouseDragged(e, dx, dy);
    }

    public boolean mouseScrolled(double x, double y, double sx, double sy) {
        return this.menu.scroll(x, y, sy != 0.0 ? sy : sx) || super.mouseScrolled(x, y, sx, sy);
    }

    public boolean keyPressed(KeyEvent e) {
        int action;
        switch (e.key()) {
            case 259: {
                int n = 1;
                break;
            }
            case 257: 
            case 335: {
                int n = 2;
                break;
            }
            case 256: {
                int n = 3;
                break;
            }
            case 258: {
                int n = 4;
                break;
            }
            case 86: {
                int n;
                if (e.hasControlDown()) {
                    n = 5;
                    break;
                }
                n = 0;
                break;
            }
            case 67: {
                int n;
                if (e.hasControlDown()) {
                    n = 6;
                    break;
                }
                n = 0;
                break;
            }
            default: {
                int n = action = 0;
            }
        }
        if (action != 0 && this.menu.key(action)) {
            return true;
        }
        return super.keyPressed(e);
    }

    public boolean charTyped(CharacterEvent e) {
        return this.menu.type(e.codepoint()) || super.charTyped(e);
    }

    public void onClose() {
        this.menu.save();
        this.back();
    }

    @Override
    public void fill(float x, float y, float w, float h, float r, int top, int bottom) {
        this.quad(Gpu.ui(0), x, y, w, h, Panel.code(r, h), 0, top, bottom);
    }

    @Override
    public void ring(float x, float y, float w, float h, float r, float t, int top, int bottom) {
        this.quad(Gpu.ui(1), x, y, w, h, Panel.code(r, h), Panel.code(t, h), top, bottom);
    }

    @Override
    public void shadow(float x, float y, float w, float h, float r, float blur, int argb) {
        float eh = h + blur * 2.0f;
        this.quad(Gpu.ui(2), x - blur, y - blur, w + blur * 2.0f, eh, Panel.code(r, eh), Panel.code(blur, eh), argb, argb);
    }

    @Override
    public void hue(float x, float y, float w, float h, float r, int argb) {
        this.quad(Gpu.ui(3), x, y, w, h, Panel.code(r, h), 0, argb, argb);
    }

    @Override
    public void sv(float x, float y, float w, float h, float r, int argb) {
        this.quad(Gpu.ui(4), x, y, w, h, Panel.code(r, h), 0, argb, argb);
    }

    @Override
    public void text(String s, float x, float y, int argb) {
        if (argb >>> 24 < 4) {
            return;
        }
        this.g.pose().pushMatrix();
        this.g.pose().translate(x, y);
        this.g.text(this.font, s, 0, 0, argb, false);
        this.g.pose().popMatrix();
    }

    @Override
    public int width(String s) {
        return this.font.width(s);
    }

    @Override
    public void totem(float x, float y, float size) {
        TextureAtlas atlas = (TextureAtlas)this.minecraft.getTextureManager().getTexture(TextureAtlas.LOCATION_ITEMS);
        TextureAtlasSprite sprite = atlas.getSprite(TOTEM);
        GpuTextureView view = atlas.getTextureView();
        this.element(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture((GpuTextureView)view, (GpuSampler)Gpu.nearest()), x, y, size, size, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), -1, -1);
        if (!Halo.want()) {
            return;
        }
        Halo.Result r = Halo.bake(Gpu.wrap(view), sprite.getU0(), sprite.getV1(), sprite.getU1(), sprite.getV0(), sprite);
        if (r == null) {
            return;
        }
        float p = (float)r.pad() * size / 16.0f;
        this.element(Halo.pipeline().get(), TextureSetup.singleTexture((GpuTextureView)r.view().view(), (GpuSampler)Gpu.linear()), x - p, y - p, size + 2.0f * p, size + 2.0f * p, 0.0f, 1.0f, 1.0f, 0.0f, -1, -1);
    }

    private void quad(RenderPipeline p, float x, float y, float w, float h, int cu, int cv, int top, int bottom) {
        if (w <= 0.0f || h <= 0.0f || (top | bottom) >>> 24 == 0) {
            return;
        }
        this.element(p, TextureSetup.noTexture(), x, y, w, h, 4 * cu + 1, 4 * cu + 3, 4 * cv + 1, 4 * cv + 3, top, bottom);
    }

    private void element(RenderPipeline p, TextureSetup t, float x, float y, float w, float h, float u0, float u1, float v0, float v1, int top, int bottom) {
        Matrix3x2f pose = new Matrix3x2f((Matrix3x2fc)this.g.pose()).translate(x, y).scale(w, h);
        this.g.guiRenderState.addGuiElement((GuiElementRenderState)new Quad(p, t, pose, u0, u1, v0, v1, top, bottom, new ScreenRectangle(0, 0, 1, 1).transformMaxBounds((Matrix3x2fc)pose)));
    }

    private static int code(float v, float h) {
        return Math.max(0, Math.min(4000, Math.round(v / Math.max(h, 0.001f) * 500.0f)));
    }

    private record Quad(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float u0, float u1, float v0, float v1, int top, int bottom, ScreenRectangle bounds) implements GuiElementRenderState
    {
        public void buildVertices(VertexConsumer c) {
            c.addVertexWith2DPose((Matrix3x2fc)this.pose, 0.0f, 0.0f).setUv(this.u0, this.v0).setColor(this.top);
            c.addVertexWith2DPose((Matrix3x2fc)this.pose, 0.0f, 1.0f).setUv(this.u0, this.v1).setColor(this.bottom);
            c.addVertexWith2DPose((Matrix3x2fc)this.pose, 1.0f, 1.0f).setUv(this.u1, this.v1).setColor(this.bottom);
            c.addVertexWith2DPose((Matrix3x2fc)this.pose, 1.0f, 0.0f).setUv(this.u1, this.v0).setColor(this.top);
        }

        public ScreenRectangle scissorArea() {
            return null;
        }
    }
}

