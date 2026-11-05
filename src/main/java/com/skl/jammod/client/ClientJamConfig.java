package com.skl.jammod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.skl.jammod.JamMod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientJamConfig {
    private static final Path CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("jammod_client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public boolean overlayEnabled = true;
        public OverlayCorner overlayCorner = OverlayCorner.TOP_RIGHT;
    }

    static {
        load();
    }

    public static synchronized void load() {
        if (Files.exists(CONFIG_FILE)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                    if (data.overlayCorner == null) data.overlayCorner = OverlayCorner.TOP_RIGHT;
                }
            } catch (Exception e) {
                JamMod.LOGGER.error("[JamMod] Failed to load client config: ", e);
            }
        }
    }

    public static synchronized void save() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
            GSON.toJson(data, writer);
        } catch (Exception e) {
            JamMod.LOGGER.error("[JamMod] Failed to save client config: ", e);
        }
    }

    public static boolean isOverlayEnabled() {
        return data.overlayEnabled;
    }

    public static void setOverlayEnabled(boolean enabled) {
        data.overlayEnabled = enabled;
        save();
    }

    public static void toggleOverlay() {
        setOverlayEnabled(!data.overlayEnabled);
    }

    public static OverlayCorner getOverlayCorner() {
        return data.overlayCorner != null ? data.overlayCorner : OverlayCorner.TOP_RIGHT;
    }

    public static void setOverlayCorner(OverlayCorner corner) {
        data.overlayCorner = corner;
        save();
    }

    public static void cycleOverlayCorner() {
        setOverlayCorner(getOverlayCorner().next());
    }
}