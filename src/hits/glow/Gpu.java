/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.GpuFormat
 *  com.mojang.blaze3d.PrimitiveTopology
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.pipeline.BindGroupLayout
 *  com.mojang.blaze3d.pipeline.BindGroupLayout$Builder
 *  com.mojang.blaze3d.pipeline.BlendFunction
 *  com.mojang.blaze3d.pipeline.ColorTargetState
 *  com.mojang.blaze3d.pipeline.DepthStencilState
 *  com.mojang.blaze3d.pipeline.RenderPipeline
 *  com.mojang.blaze3d.pipeline.RenderPipeline$Snippet
 *  com.mojang.blaze3d.pipeline.RenderTarget
 *  com.mojang.blaze3d.platform.CompareOp
 *  com.mojang.blaze3d.shaders.UniformType
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderPass
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.FilterMode
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.BindGroupLayouts
 *  net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState
 *  net.minecraft.client.renderer.rendertype.OutputTarget
 *  net.minecraft.client.renderer.rendertype.PreparedRenderType$Texture
 *  net.minecraft.client.renderer.rendertype.RenderSetup
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.renderer.texture.TextureAtlas
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.resources.Identifier
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryStack
 */
package hits.glow;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import hits.glow.Bloom;
import hits.glow.Glow;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

public final class Gpu {
    public static final int NONE = 0;
    public static final int PRE = 1;
    private static final OutputTarget OUT = new OutputTarget("totemglow_mask", () -> target);
    private static final Map<RenderType, RenderType> MASKS = new IdentityHashMap<RenderType, RenderType>();
    private static final RenderPipeline[] UI = new RenderPipeline[5];
    private static final Vector4f CLEAR = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
    private static RenderPipeline mask;
    private static Target target;
    private static Tex maskTex;
    private static Tex screenTex;
    private static Tex wrapped;

    private Gpu() {
    }

    private static RenderTarget main() {
        return Minecraft.getInstance().gameRenderer.mainRenderTarget();
    }

    public static boolean frame() {
        RenderTarget main = Gpu.main();
        if (main.width <= 0 || main.height <= 0) {
            return false;
        }
        if (target == null) {
            target = new Target();
        }
        if (Gpu.target.width != main.width || Gpu.target.height != main.height || target.getColorTexture() == null) {
            target.resize(main.width, main.height);
        }
        return true;
    }

    public static void clearMask() {
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), (Vector4fc)CLEAR);
    }

    public static Tex mask() {
        GpuTextureView v = target.getColorTextureView();
        if (maskTex == null || Gpu.maskTex.view != v) {
            maskTex = new Tex(null, v);
        }
        return maskTex;
    }

    public static Tex screen() {
        GpuTextureView v = Gpu.main().getColorTextureView();
        if (screenTex == null || Gpu.screenTex.view != v) {
            screenTex = new Tex(null, v);
        }
        return screenTex;
    }

    public static Tex wrap(GpuTextureView v) {
        if (wrapped == null || Gpu.wrapped.view != v) {
            wrapped = new Tex(null, v);
        }
        return wrapped;
    }

    public static void tag(ItemStackRenderState.LayerRenderState layer) {
        Bloom.tag(layer.prepareQuadList());
    }

    public static boolean redirected() {
        return RenderSystem.outputColorTextureOverride != null;
    }

    private static ColorTargetState color(int blend) {
        if (blend == 0) {
            return new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, 15);
        }
        return new ColorTargetState(Optional.of(BlendFunction.TRANSLUCENT_PREMULTIPLIED_ALPHA), GpuFormat.RGBA8_UNORM, 7);
    }

    public static Pipe post(String frag, String block, int blend, boolean masked) {
        BindGroupLayout.Builder layout = BindGroupLayout.builder().withSampler("InSampler").withUniform(block, UniformType.UNIFORM_BUFFER);
        if (masked) {
            layout.withSampler("MaskSampler");
        }
        return new Pipe(RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Glow.id("pipeline/" + frag + "_" + blend)).withVertexShader(Glow.id("core/quad")).withFragmentShader(Glow.id("core/" + frag)).withBindGroupLayout(layout.build()).withCull(false).withColorTargetState(Gpu.color(blend)).withPrimitiveTopology(PrimitiveTopology.TRIANGLES).build());
    }

    public static RenderPipeline ui(int mode) {
        if (UI[mode] == null) {
            Gpu.UI[mode] = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Glow.id("pipeline/ui_" + mode)).withVertexShader(Glow.id("core/ui")).withFragmentShader(Glow.id("core/ui")).withShaderDefine("MODE", mode).withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).build();
        }
        return UI[mode];
    }

    public static Pipe halo() {
        return new Pipe(RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Glow.id("pipeline/halo")).withVertexShader(Glow.id("core/ui")).withFragmentShader(Glow.id("core/halo")).withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).withBindGroupLayout(BindGroupLayouts.SAMPLER0).withColorTargetState(Gpu.color(1)).withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR).withPrimitiveTopology(PrimitiveTopology.QUADS).build());
    }

    private static RenderPipeline maskPipe() {
        if (mask == null) {
            mask = RenderPipeline.builder((RenderPipeline.Snippet[])new RenderPipeline.Snippet[0]).withLocation(Glow.id("pipeline/mask")).withVertexShader(Glow.id("core/mask")).withFragmentShader(Glow.id("core/mask")).withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).withBindGroupLayout(BindGroupLayouts.SAMPLER0).withColorTargetState(Gpu.color(0)).withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false)).withVertexBinding(0, DefaultVertexFormat.ENTITY).withPrimitiveTopology(PrimitiveTopology.QUADS).build();
        }
        return mask;
    }

    public static RenderType maskType(RenderType src) {
        if (MASKS.containsKey(src)) {
            return MASKS.get(src);
        }
        Identifier atlas = Gpu.atlasOf(src);
        RenderType t = atlas == null ? null : RenderType.create((String)"totemglow_mask", (RenderSetup)RenderSetup.builder((RenderPipeline)Gpu.maskPipe()).withTexture("Sampler0", atlas).setOutputTarget(OUT).createRenderSetup());
        MASKS.put(src, t);
        return t;
    }

    private static Identifier atlasOf(RenderType src) {
        GpuTextureView bound = null;
        for (PreparedRenderType.Texture t : src.prepare().textures()) {
            if (!"Sampler0".equals(t.name())) continue;
            bound = t.textureView();
        }
        if (bound == null) {
            return null;
        }
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        if (bound == tm.getTexture(TextureAtlas.LOCATION_ITEMS).getTextureView()) {
            return TextureAtlas.LOCATION_ITEMS;
        }
        if (bound == tm.getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView()) {
            return TextureAtlas.LOCATION_BLOCKS;
        }
        return null;
    }

    public static Tex texture(String label, int w, int h) {
        GpuDevice device = RenderSystem.getDevice();
        GpuTexture t = device.createTexture(label, 15, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
        return new Tex(t, device.createTextureView(t));
    }

    public static void draw(Pipe p, Tex out, String block, Buf ubo, Tex in, Tex mask) {
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "totemglow", out.view, Optional.empty());){
            pass.setPipeline(p.pipeline);
            pass.setUniform(block, ubo.buffer);
            pass.bindTexture("InSampler", in.view, Gpu.linear());
            if (mask != null) {
                pass.bindTexture("MaskSampler", mask.view, Gpu.nearest());
            }
            pass.draw(3, 1, 0, 0);
        }
    }

    public static Buf ubo(String label, int size) {
        return new Buf(RenderSystem.getDevice().createBuffer(() -> label, 136, (long)size));
    }

    public static void write(Buf ubo, float ... values) {
        try (MemoryStack stack = MemoryStack.stackPush();){
            ByteBuffer data = stack.malloc(values.length * 4);
            for (float v : values) {
                data.putFloat(v);
            }
            data.flip();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(ubo.buffer.slice(), data);
        }
    }

    public static int guiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    public static GpuSampler linear() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
    }

    public static GpuSampler nearest() {
        return RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
    }

    private static final class Target
    extends RenderTarget {
        Target() {
            super("totemglow mask", true, GpuFormat.RGBA8_UNORM);
        }

        public void createBuffers(int w, int h) {
            RenderSystem.assertOnRenderThread();
            GpuDevice device = RenderSystem.getDevice();
            this.width = w;
            this.height = h;
            this.colorTexture = device.createTexture(() -> this.label + " / Color", 15, this.format, w, h, 1, 1);
            this.colorTextureView = device.createTextureView(this.colorTexture);
        }

        public GpuTexture getDepthTexture() {
            return Gpu.main().getDepthTexture();
        }

        public GpuTextureView getDepthTextureView() {
            return Gpu.main().getDepthTextureView();
        }
    }

    public static final class Tex {
        private final GpuTexture texture;
        private final GpuTextureView view;

        Tex(GpuTexture texture, GpuTextureView view) {
            this.texture = texture;
            this.view = view;
        }

        public GpuTextureView view() {
            return this.view;
        }

        public int width() {
            return this.view.texture().getWidth(0);
        }

        public int height() {
            return this.view.texture().getHeight(0);
        }

        public void close() {
            if (this.texture == null) {
                return;
            }
            this.view.close();
            this.texture.close();
        }
    }

    public static final class Pipe {
        private final RenderPipeline pipeline;

        Pipe(RenderPipeline pipeline) {
            this.pipeline = pipeline;
        }

        public RenderPipeline get() {
            return this.pipeline;
        }
    }

    public static final class Buf {
        private final GpuBuffer buffer;

        Buf(GpuBuffer buffer) {
            this.buffer = buffer;
        }
    }
}

