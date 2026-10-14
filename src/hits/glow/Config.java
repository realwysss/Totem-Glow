/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonParseException
 */
package hits.glow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import hits.glow.Anim;
import hits.glow.Colors;
import hits.glow.Glow;
import hits.glow.Layer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;

public final class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config now = new Config();
    private static Path file;
    public boolean enabled = true;
    public Layer eyes = new Layer(0x55FF55, 2.0f, 1.0f, 0.7f);
    public Layer body = new Layer(16766720, 3.0f, 1.0f, 0.85f, false);
    public Anim anim = Anim.STATIC;
    public float speed = 1.0f;
    public float inner = 0.25f;
    public boolean screen = false;
    public boolean fast = false;
    public boolean hand = true;
    public boolean gui = true;
    public boolean world = true;
    public boolean pop = true;

    public static Config get() {
        return now;
    }

    public static void set(Config c) {
        c.fix();
        now = c;
    }

    public Config copy() {
        Config c = (Config)GSON.fromJson(GSON.toJson((Object)this), Config.class);
        c.fix();
        return c;
    }

    public boolean same(Config o) {
        return o != null && GSON.toJson((Object)this).equals(GSON.toJson((Object)o));
    }

    public void fix() {
        if (this.eyes == null) {
            this.eyes = new Layer(0x55FF55, 2.0f, 1.0f, 0.7f);
        }
        if (this.body == null) {
            this.body = new Layer(16766720, 3.0f, 1.0f, 0.85f, false);
        }
        if (this.anim == null) {
            this.anim = Anim.STATIC;
        }
        this.eyes.fix();
        this.body.fix();
        this.speed = Float.isNaN(this.speed) ? 1.0f : Colors.clamp(this.speed, 0.1f, 4.0f);
        this.inner = Float.isNaN(this.inner) ? 0.25f : Colors.clamp(this.inner, 0.0f, 1.0f);
    }

    public boolean live() {
        return this.enabled && (this.eyes.live() || this.body.live());
    }

    public static void load(Path path) {
        file = path;
        Config c = null;
        if (Files.isRegularFile(path, new LinkOption[0])) {
            try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8);){
                c = (Config)GSON.fromJson((Reader)r, Config.class);
            }
            catch (JsonParseException | IOException | IllegalStateException e) {
                Glow.LOG.warn("bad config, using defaults: {}", (Object)e.getMessage());
            }
        }
        Config.set(c == null ? new Config() : c);
        if (c == null) {
            Config.save();
        }
    }

    public static void save() {
        if (file == null) {
            return;
        }
        try {
            Files.createDirectories(file.getParent(), new FileAttribute[0]);
            Path tmp = file.resolveSibling(String.valueOf(file.getFileName()) + ".tmp");
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8, new OpenOption[0]);){
                GSON.toJson((Object)now, (Appendable)w);
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (IOException e) {
            Glow.LOG.warn("couldnt save config: {}", (Object)e.getMessage());
        }
    }
}

