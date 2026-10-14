/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.QuadInstance
 *  net.minecraft.client.renderer.feature.ItemFeatureRenderer
 *  net.minecraft.client.renderer.feature.ItemFeatureRenderer$Submit
 *  net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer
 *  net.minecraft.client.renderer.rendertype.RenderType
 *  net.minecraft.client.resources.model.geometry.BakedQuad
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import com.mojang.blaze3d.vertex.QuadInstance;
import hits.glow.Bloom;
import hits.glow.Gpu;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemFeatureRenderer.class})
abstract class FeatureMix
extends RenderTypeFeatureRenderer<ItemFeatureRenderer.Submit> {
    @Shadow
    @Final
    private QuadInstance quadInstance;

    FeatureMix() {
    }

    @Inject(method={"prepareMainSubmit"}, at={@At(value="TAIL")})
    private void tg$mask(ItemFeatureRenderer.Submit submit, CallbackInfo ci) {
        if (!Bloom.tagged(submit.quads())) {
            return;
        }
        for (BakedQuad quad : submit.quads()) {
            RenderType m = Gpu.maskType(quad.materialInfo().itemRenderType());
            if (m == null) continue;
            this.getVertexBuilder(m).putBakedQuad(submit.pose(), quad, this.quadInstance);
        }
    }
}

