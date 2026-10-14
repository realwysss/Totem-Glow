/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState
 *  net.minecraft.world.item.ItemDisplayContext
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import hits.glow.Bloom;
import hits.glow.Gpu;
import hits.glow.Tag;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemStackRenderState.class})
abstract class StackMix
implements Tag {
    @Shadow
    ItemDisplayContext displayContext;
    @Shadow
    private int activeLayerCount;
    @Shadow
    private ItemStackRenderState.LayerRenderState[] layers;
    @Unique
    private boolean tg$totem;

    StackMix() {
    }

    @Override
    public boolean tg$totem() {
        return this.tg$totem;
    }

    @Override
    public void tg$totem(boolean totem) {
        this.tg$totem = totem;
    }

    @Inject(method={"clear"}, at={@At(value="HEAD")})
    private void tg$clear(CallbackInfo ci) {
        this.tg$totem = false;
    }

    @Inject(method={"submit"}, at={@At(value="HEAD")})
    private void tg$submit(CallbackInfo ci) {
        if (!this.tg$totem || !Bloom.want(this.displayContext)) {
            return;
        }
        for (int i = 0; i < this.activeLayerCount; ++i) {
            Gpu.tag(this.layers[i]);
        }
    }
}

