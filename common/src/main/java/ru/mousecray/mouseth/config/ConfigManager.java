package ru.mousecray.mouseth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.mousecray.mouseth.Constants;
import ru.mousecray.mouseth.platform.Services;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Gson          GSON     = new GsonBuilder().setPrettyPrinting().create();
    private static       MouseTHConfig INSTANCE = new MouseTHConfig();
    private static       Path          configPath;

    public static synchronized void load() {
        try {
            configPath = Services.PLATFORM.getConfigDir().resolve("mouseth.json");
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    MouseTHConfig loaded = GSON.fromJson(reader, MouseTHConfig.class);
                    if (loaded != null) INSTANCE = loaded;
                }
            } else save();
        } catch (Exception e) { Constants.LOGGER.error("Failed to load config", e); }
    }

    public static synchronized void save() {
        try {
            if (configPath == null) configPath = Services.PLATFORM.getConfigDir().resolve("mouseth.json");
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) { GSON.toJson(INSTANCE, writer); }
        } catch (Exception e) { Constants.LOGGER.error("Failed to save config", e); }
    }

    public static MouseTHConfig get() {
        if (configPath == null) load();
        return INSTANCE;
    }
}