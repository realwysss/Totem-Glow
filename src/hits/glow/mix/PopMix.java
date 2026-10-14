/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.ScreenEffectRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import hits.glow.Bloom;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ScreenEffectRenderer.class})
abstract class PopMix {
    PopMix() {
    }

    @Inject(method={"renderItemActivationAnimation"}, at={@At(value="HEAD")})
    private void tg$in(CallbackInfo ci) {
        Bloom.pop = true;
    }

    @Inject(method={"renderItemActivationAnimation"}, at={@At(value="RETURN")})
    private void tg$out(CallbackInfo ci) {
        Bloom.pop = false;
    }
}

