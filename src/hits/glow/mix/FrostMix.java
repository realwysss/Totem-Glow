/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.GameRenderer
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import hits.glow.Frost;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GameRenderer.class})
abstract class FrostMix {
    FrostMix() {
    }

    @Inject(method={"processBlurEffect"}, at={@At(value="HEAD")}, cancellable=true)
    private void tg$frost(CallbackInfo ci) {
        if (Frost.run()) {
            ci.cancel();
        }
    }
}

