package ru.mousecray.mouseth.patch;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RawTranslationsLoader {
    private static final Gson                                                       GSON              = new Gson();
    private static final Map<String, Map<String, Map<String, Map<String, String>>>> TRANSLATIONS      = new ConcurrentHashMap<>();
    private static       boolean                                                    initialized       = false;
    private static       boolean                                                    enabled           = false;
    private static       boolean                                                    bindToLineNumbers = false;

    public static synchronized void init() { init(new File(System.getProperty("user.dir"))); }

    public static synchronized void init(File gameDir) {
        if (initialized) return;
        initialized = true;

        File configFile = new File(gameDir, "config/mouseth.json");
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
            JsonObject config = GSON.fromJson(reader, JsonObject.class);
            if (config != null && config.has("loadMode") && config.get("loadMode").getAsBoolean() &&
                    config.has("translateRawStrings") && config.get("translateRawStrings").getAsBoolean()) {

                enabled = true;
                bindToLineNumbers = config.has("bindToLineNumbers") && config.get("bindToLineNumbers").getAsBoolean();
                String targetLang = config.has("targetLanguage") ? config.get("targetLanguage").getAsString() : "en_us";

                File baseMouseTH = new File(gameDir, "MouseTH");
                if (baseMouseTH.exists() && baseMouseTH.isDirectory()) {
                    File[] subDirs = baseMouseTH.listFiles(File::isDirectory);
                    if (subDirs != null) {
                        for (File subDir : subDirs) {
                            File rawFile = new File(subDir, targetLang + "/raw.json");
                            if (rawFile.exists()) {
                                loadRawFile(rawFile);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    private static void loadRawFile(File rawFile) {
        try (FileReader reader = new FileReader(rawFile, StandardCharsets.UTF_8)) {
            JsonObject rawJson = GSON.fromJson(reader, JsonObject.class);
            if (rawJson == null) return;

            for (Map.Entry<String, JsonElement> classEntry : rawJson.entrySet()) {
                String                                        className  = classEntry.getKey().replace('.', '/');
                Map<String, Map<String, Map<String, String>>> methodsMap = TRANSLATIONS.computeIfAbsent(className, k -> new ConcurrentHashMap<>());

                for (Map.Entry<String, JsonElement> methodEntry : classEntry.getValue().getAsJsonObject().entrySet()) {
                    String                           methodSig    = methodEntry.getKey();
                    Map<String, Map<String, String>> selectorsMap = methodsMap.computeIfAbsent(methodSig, k -> new ConcurrentHashMap<>());

                    for (Map.Entry<String, JsonElement> selectorEntry : methodEntry.getValue().getAsJsonObject().entrySet()) {
                        String              selector   = selectorEntry.getKey();
                        Map<String, String> stringsMap = selectorsMap.computeIfAbsent(selector, k -> new ConcurrentHashMap<>());

                        for (Map.Entry<String, JsonElement> strEntry : selectorEntry.getValue().getAsJsonObject().entrySet()) {
                            stringsMap.put(strEntry.getKey(), strEntry.getValue().getAsString());
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    public static boolean isEnabled() {
        if (!initialized) init();
        return enabled;
    }

    public static boolean isBindToLineNumbers() {
        if (!initialized) init();
        return bindToLineNumbers;
    }

    public static boolean hasClassTranslations(String className) {
        if (!isEnabled()) return false;
        String normalized = className.replace('.', '/');
        return TRANSLATIONS.containsKey(normalized);
    }

    public static Map<String, Map<String, Map<String, String>>> getMethodTranslations(String className) {
        if (!isEnabled()) return null;
        String normalized = className.replace('.', '/');
        return TRANSLATIONS.get(normalized);
    }
}