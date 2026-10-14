/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.resources.Identifier
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package hits.glow;

import hits.glow.Config;
import hits.glow.Keys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Glow
implements ClientModInitializer {
    public static final String ID = "totemglow";
    public static final Logger LOG = LoggerFactory.getLogger((String)"Totem Glow");
    private static final long START = System.nanoTime();

    public void onInitializeClient() {
        Config.load(FabricLoader.getInstance().getConfigDir().resolve("totemglow.json"));
        Keys.register();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath((String)ID, (String)path);
    }

    public static double seconds() {
        return (double)(System.nanoTime() - START) / 1.0E9;
    }
}

