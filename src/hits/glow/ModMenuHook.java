/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.terraformersmc.modmenu.api.ConfigScreenFactory
 *  com.terraformersmc.modmenu.api.ModMenuApi
 */
package hits.glow;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import hits.glow.ui.Panel;

public final class ModMenuHook
implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return Panel::new;
    }
}

