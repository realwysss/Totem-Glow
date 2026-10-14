/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.textures.GpuSampler
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  net.minecraft.client.gui.render.GuiItemAtlas$SlotView
 *  net.minecraft.client.gui.render.GuiRenderer
 *  net.minecraft.client.gui.render.TextureSetup
 *  net.minecraft.client.renderer.item.TrackingItemStackRenderState
 *  net.minecraft.client.renderer.state.gui.BlitRenderState
 *  net.minecraft.client.renderer.state.gui.GuiItemRenderState
 *  net.minecraft.client.renderer.state.gui.GuiRenderState
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import hits.glow.Gpu;
import hits.glow.Halo;
import hits.glow.Tag;
import net.minecraft.client.gui.render.GuiItemAtlas;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiRenderer.class})
abstract class GuiMix {
    @Shadow
    @Final
    private GuiRenderState renderState;

    GuiMix() {
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void tg$frame(CallbackInfo ci) {
        Halo.tick();
    }

    @Inject(method={"submitBlitFromItemAtlas"}, at={@At(value="TAIL")})
    private void tg$halo(GuiItemRenderState item, GuiItemAtlas.SlotView slot, CallbackInfo ci) {
        TrackingItemStackRenderState state = item.itemStackRenderState();
        if (!((Tag)state).tg$totem() || !Halo.want()) {
            return;
        }
        Halo.Result r = Halo.bake(Gpu.wrap(slot.textureView()), slot.u0(), slot.v1(), slot.u1(), slot.v0(), state.getModelIdentity());
        if (r == null) {
            return;
        }
        int p = r.pad();
        this.renderState.addBlitToCurrentLayer(new BlitRenderState(Halo.pipeline().get(), TextureSetup.singleTexture((GpuTextureView)r.view().view(), (GpuSampler)Gpu.linear()), item.pose(), item.x() - p, item.y() - p, item.x() + 16 + p, item.y() + 16 + p, 0.0f, 1.0f, 1.0f, 0.0f, -1, item.scissorArea()));
    }
}

