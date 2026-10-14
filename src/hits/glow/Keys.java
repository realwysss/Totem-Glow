/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.KeyMapping$Category
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.resources.Identifier
 */
package hits.glow;

import hits.glow.Glow;
import hits.glow.ui.Panel;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;

public final class Keys {
    private static KeyMapping open;

    private Keys() {
    }

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register((Identifier)Glow.id("main"));
        open = KeyMappingHelper.registerKeyMapping((KeyMapping)new KeyMapping("key.totemglow.open", 75, category));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (open.consumeClick()) {
                if (mc.gui.screen() != null) continue;
                mc.gui.setScreen((Screen)new Panel(null));
            }
        });
    }
}

