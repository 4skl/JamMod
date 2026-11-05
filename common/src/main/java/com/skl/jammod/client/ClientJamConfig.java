package com.skl.jammod.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientJamConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("JamMod");
    private static Path configFile = Paths.get("config", "jammod_client.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static ConfigData data = new ConfigData();

    public static class ConfigData {
        public boolean overlayEnabled = true;
        public OverlayCorner overlayCorner = OverlayCorner.TOP_RIGHT;
    }

    static {
        load();
    }

    public static synchronized void setConfigDirectory(Path dir) {
        configFile = dir.resolve("jammod_client.json");
        load();
    }

    public static synchronized void load() {
        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
                if (loaded != null) {
                    data = loaded;
                    if (data.overlayCorner == null) data.overlayCorner = OverlayCorner.TOP_RIGHT;
                }
            } catch (Exception e) {
                LOGGER.error("[JamMod] Failed to load client config: ", e);
            }
        }
    }

    public static synchronized void save() {
        try {
            if (configFile.getParent() != null) {
                Files.createDirectories(configFile.getParent());
            }
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            LOGGER.error("[JamMod] Failed to save client config: ", e);
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
