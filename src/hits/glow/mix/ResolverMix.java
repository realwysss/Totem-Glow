/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.item.ItemModelResolver
 *  net.minecraft.client.renderer.item.ItemStackRenderState
 *  net.minecraft.world.entity.ItemOwner
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package hits.glow.mix;

import hits.glow.Tag;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ItemModelResolver.class})
abstract class ResolverMix {
    ResolverMix() {
    }

    @Inject(method={"updateForTopItem"}, at={@At(value="TAIL")})
    private void tg$resolve(ItemStackRenderState state, ItemStack stack, ItemDisplayContext ctx, Level level, ItemOwner owner, int seed, CallbackInfo ci) {
        ((Tag)state).tg$totem(!stack.isEmpty() && stack.is((Object)Items.TOTEM_OF_UNDYING));
    }
}

