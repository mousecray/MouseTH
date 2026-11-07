package ru.mousecray.mouseth.patch;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.IoSupplier;
import ru.mousecray.mouseth.Constants;
import ru.mousecray.mouseth.MouseTH;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.platform.Services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class RawJsonPatcher {
    private static final Gson                             GSON              = new GsonBuilder().create();
    private static final Map<String, Map<String, String>> FILE_TRANSLATIONS = new ConcurrentHashMap<>();

    public static void init() {
        MouseTHConfig config = ConfigManager.get();
        if (config.loadMode && config.translateJsonRawStrings) {
            Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
            loadTranslationsFromBaseDir(baseDir, config.targetLanguage, config.loadResourcePacks);
        }
    }

    public static void enableForSession(String targetLanguage, boolean allowExternalPacks) {
        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        loadTranslationsFromBaseDir(baseDir, targetLanguage, allowExternalPacks);
    }

    public static void loadWorldTranslations(String targetLanguage) {
        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        loadTranslationsFromBaseDir(baseDir, targetLanguage, true);
    }

    private static void loadTranslationsFromBaseDir(Path baseDir, String targetLanguage, boolean allowExternalPacks) {
        if (!Files.isDirectory(baseDir)) return;

        Map<String, Path> loadedMods = Services.PLATFORM.getLoadedMods();

        try (Stream<Path> stream = Files.list(baseDir)) {
            stream.filter(Files::isDirectory).forEach(idDir -> {
                String id = idDir.getFileName().toString();
                if (!loadedMods.containsKey(id) && !allowExternalPacks && !id.equals("minecraft")) return;

                Path rawJsonFile = idDir.resolve(targetLanguage).resolve("rawjson.json");
                if (Files.exists(rawJsonFile)) {
                    try (Reader reader = Files.newBufferedReader(rawJsonFile, StandardCharsets.UTF_8)) {
                        JsonObject obj = GSON.fromJson(reader, JsonObject.class);
                        if (obj != null) {
                            for (Map.Entry<String, JsonElement> fileEntry : obj.entrySet()) {
                                if (fileEntry.getValue().isJsonObject()) {
                                    Map<String, String> replacements = FILE_TRANSLATIONS.computeIfAbsent(
                                            fileEntry.getKey().replace('\\', '/'),
                                            k -> new ConcurrentHashMap<>()
                                    );
                                    for (Map.Entry<String, JsonElement> strEntry : fileEntry.getValue().getAsJsonObject().entrySet()) {
                                        if (strEntry.getValue().isJsonPrimitive()) {
                                            replacements.put(strEntry.getKey(), strEntry.getValue().getAsString());
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) { Constants.LOGGER.error("Failed to load rawjson for {}", id, e); }
                }
            });
        } catch (Exception e) { Constants.LOGGER.error("Failed to walk MouseTH directory for rawjson", e); }
    }

    public static IoSupplier<InputStream> wrapSupplier(ResourceLocation location, IoSupplier<InputStream> original) {
        MouseTHConfig config = ConfigManager.get();
        if ((!config.loadMode && !MouseTH.isSessionActive()) || !config.translateJsonRawStrings) return original;

        String path = location.getPath();
        if (!path.endsWith(".json")) return original;

        String assetKey = "assets/" + location.getNamespace() + "/" + path;
        String dataKey  = "data/" + location.getNamespace() + "/" + path;

        Map<String, String> replacements = FILE_TRANSLATIONS.get(assetKey);
        if (replacements == null) replacements = FILE_TRANSLATIONS.get(dataKey);
        if (replacements == null || replacements.isEmpty()) return original;

        Map<String, String> finalReplacements = replacements;
        return () -> patchStream(original.get(), finalReplacements);
    }

    private static InputStream patchStream(InputStream input, Map<String, String> replacements) {
        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            JsonElement root = GSON.fromJson(reader, JsonElement.class);
            if (root != null) {
                replaceInElement(root, replacements);
                byte[] bytes = GSON.toJson(root).getBytes(StandardCharsets.UTF_8);
                return new ByteArrayInputStream(bytes);
            }
        } catch (Exception ignored) { }
        return input;
    }

    private static void replaceInElement(JsonElement element, Map<String, String> replacements) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) return;

        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (JsonElement child : array) replaceInElement(child, replacements);
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("text") && obj.get("text").isJsonPrimitive()) {
                String original   = obj.get("text").getAsString();
                String translated = replacements.get(original);
                if (translated != null) obj.addProperty("text", translated);
            }
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) replaceInElement(entry.getValue(), replacements);
        }
    }
}