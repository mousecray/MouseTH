package ru.mousecray.mouseth.scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.objectweb.asm.ClassReader;
import ru.mousecray.mouseth.Constants;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.mixin.ClientLanguageAccessor;
import ru.mousecray.mouseth.platform.Services;
import ru.mousecray.mouseth.utils.ExportData;
import ru.mousecray.mouseth.utils.Stats;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MouseTHScanner {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public static Stats runExtraction(String targetLang) {
        Stats                   stats   = new Stats();
        MouseTHConfig           config  = ConfigManager.get();
        ResourceManager         rm      = Minecraft.getInstance().getResourceManager();
        Language                lang    = Language.getInstance();
        Map<String, ExportData> dataMap = new HashMap<>();

        if (config.extractMissingTranslations) {
            for (String namespace : rm.getNamespaces()) {
                if (namespace.equals("minecraft")) continue;

                ResourceLocation enUsLoc   = ResourceLocation.tryBuild(namespace, "lang/en_us.json");
                ResourceLocation targetLoc = ResourceLocation.tryBuild(namespace, "lang/" + targetLang + ".json");

                if (enUsLoc == null || targetLoc == null) continue;

                JsonObject enUsObj   = loadJsonStackFromResource(rm, enUsLoc);
                JsonObject targetObj = loadJsonStackFromResource(rm, targetLoc);

                if (enUsObj != null) {
                    ExportData data = dataMap.computeIfAbsent(namespace, k -> new ExportData());
                    for (String key : enUsObj.keySet()) {
                        if (targetObj == null || !targetObj.has(key)) {
                            data.getTranslations().add(key, enUsObj.get(key));
                            stats.setKeysFound(stats.getKeysFound() + 1);
                        }
                    }
                }
            }
        }

        if (config.extractMissingKeys) {
            //noinspection deprecation
            for (var entry : BuiltInRegistries.ITEM.entrySet()) {
                ResourceLocation loc = entry.getKey().location();
                if (loc.getNamespace().equals("minecraft")) continue;

                String     key  = entry.getValue().getDescriptionId();
                ExportData data = dataMap.computeIfAbsent(loc.getNamespace(), k -> new ExportData());
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }

            //noinspection deprecation
            for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
                ResourceLocation loc = entry.getKey().location();
                if (loc.getNamespace().equals("minecraft")) continue;

                String     key  = entry.getValue().getDescriptionId();
                ExportData data = dataMap.computeIfAbsent(loc.getNamespace(), k -> new ExportData());
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }

            //noinspection deprecation
            for (var entry : BuiltInRegistries.ENTITY_TYPE.entrySet()) {
                ResourceLocation loc = entry.getKey().location();
                if (loc.getNamespace().equals("minecraft")) continue;

                String     key  = entry.getValue().getDescriptionId();
                ExportData data = dataMap.computeIfAbsent(loc.getNamespace(), k -> new ExportData());
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }

            //noinspection deprecation
            for (var entry : BuiltInRegistries.MOB_EFFECT.entrySet()) {
                ResourceLocation loc = entry.getKey().location();
                if (loc.getNamespace().equals("minecraft")) continue;

                String     key  = entry.getValue().getDescriptionId();
                ExportData data = dataMap.computeIfAbsent(loc.getNamespace(), k -> new ExportData());
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }

            //noinspection deprecation
            for (var entry : BuiltInRegistries.ENCHANTMENT.entrySet()) {
                ResourceLocation loc = entry.getKey().location();
                if (loc.getNamespace().equals("minecraft")) continue;

                String     key  = entry.getValue().getDescriptionId();
                ExportData data = dataMap.computeIfAbsent(loc.getNamespace(), k -> new ExportData());
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }
        }

        if (config.extractRawStrings) {
            Map<String, Path> mods = Services.PLATFORM.getLoadedMods();
            for (Map.Entry<String, Path> entry : mods.entrySet()) {
                String modId = entry.getKey();
                if (modId.equals(Constants.MOD_ID)) continue;

                Path       root    = entry.getValue();
                ExportData modData = dataMap.computeIfAbsent(modId, k -> new ExportData());

                collectExistingKeys(root, modData);
                scanPath(root, modData, lang, config, stats);
            }
        }

        if (config.scanResourcePacks) {
            Path packsDir = Services.PLATFORM.getGameDir().resolve("resourcepacks");
            if (Files.isDirectory(packsDir)) {
                try (Stream<Path> stream = Files.list(packsDir)) {
                    stream.forEach(packPath -> {
                        String     packName = packPath.getFileName().toString();
                        String     packId   = packName.endsWith(".zip") ? packName.substring(0, packName.length() - 4) : packName;
                        ExportData packData = dataMap.computeIfAbsent(packId, k -> new ExportData());

                        if (Files.isDirectory(packPath)) {
                            collectExistingKeys(packPath, packData);
                            scanPath(packPath, packData, lang, config, stats);
                        } else if (packName.endsWith(".zip")) {
                            try (FileSystem zipFs = FileSystems.newFileSystem(packPath, Collections.emptyMap(), null)) {
                                for (Path root : zipFs.getRootDirectories()) {
                                    collectExistingKeys(root, packData);
                                    scanPath(root, packData, lang, config, stats);
                                }
                            } catch (Exception ignored) { }
                        }
                    });
                } catch (Exception e) { Constants.LOGGER.error("Failed to scan resourcepacks", e); }
            }
        }

        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        saveExportMap(baseDir, targetLang, dataMap);

        return stats;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Stats runWorldExtraction(Path worldDir, String targetLang) {
        Stats                   stats   = new Stats();
        MouseTHConfig           config  = ConfigManager.get();
        Language                lang    = Language.getInstance();
        Map<String, ExportData> dataMap = new HashMap<>();
        Path                    dpDir   = worldDir.resolve("datapacks");

        if (Files.isDirectory(dpDir)) {
            try (Stream<Path> stream = Files.list(dpDir)) {
                stream.forEach(packPath -> {
                    String     packName = packPath.getFileName().toString();
                    String     packId   = packName.endsWith(".zip") ? packName.substring(0, packName.length() - 4) : packName;
                    ExportData data     = dataMap.computeIfAbsent(packId, k -> new ExportData());

                    if (Files.isDirectory(packPath)) {
                        collectExistingKeys(packPath, data);
                        scanPath(packPath, data, lang, config, stats);
                    } else if (packName.endsWith(".zip")) {
                        try (FileSystem zipFs = FileSystems.newFileSystem(packPath, Collections.emptyMap(), null)) {
                            for (Path root : zipFs.getRootDirectories()) {
                                collectExistingKeys(root, data);
                                scanPath(root, data, lang, config, stats);
                            }
                        } catch (Exception ignored) { }
                    }
                });
            } catch (Exception e) { Constants.LOGGER.error("Failed to scan world datapacks", e); }
        }

        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        saveExportMap(baseDir, targetLang, dataMap);

        return stats;
    }

    private static void collectExistingKeys(Path root, ExportData data) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(p -> p.toString().endsWith(".json") && p.toString().contains("/lang/")).forEach(langFile -> {
                try (Reader r = new InputStreamReader(Files.newInputStream(langFile), StandardCharsets.UTF_8)) {
                    JsonObject obj = GSON.fromJson(r, JsonObject.class);
                    if (obj != null) data.getKnownKeys().addAll(obj.keySet());
                } catch (Exception ignored) { }
            });
        } catch (Exception ignored) { }
    }

    private static void scanPath(Path root, ExportData data, Language lang, MouseTHConfig config, Stats stats) {
        try (Stream<Path> walk = Files.walk(root)) {
            walk.forEach(filePath -> {
                String fileName     = filePath.toString();
                String relativePath = root.relativize(filePath).toString().replace('\\', '/');

                if (fileName.endsWith(".class")) {
                    String normalizedPath = fileName.replace('\\', '/');
                    if (normalizedPath.contains("/data/") || normalizedPath.contains("/datagen/") ||
                            normalizedPath.contains("/gametest/") || normalizedPath.endsWith("Provider.class") ||
                            normalizedPath.endsWith("Test.class")) return;

                    try (InputStream is = Files.newInputStream(filePath)) {
                        ClassReader cr        = new ClassReader(is);
                        String      className = cr.getClassName();

                        cr.accept(new MouseTHClassVisitor(
                                className, data, lang, config, stats,
                                str -> isValidRawString(
                                        str, config.minRawStringLength, config.aggressiveRawStringFiltering
                                )
                        ), ClassReader.SKIP_FRAMES);
                    } catch (Exception ignored) { }
                } else if (fileName.endsWith(".json") && !fileName.contains("/lang/")) {
                    try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8)) {
                        JsonElement jsonTree = GSON.fromJson(reader, JsonElement.class);
                        scanJsonForTextAndTranslations(jsonTree, relativePath, data, lang, config, stats);
                    } catch (Exception ignored) { }
                }
            });
        } catch (Exception ignored) { }
    }

    private static void scanJsonForTextAndTranslations(JsonElement element, String relativePath, ExportData data, Language lang, MouseTHConfig config, Stats stats) {
        if (element == null || element.isJsonNull() || element.isJsonPrimitive()) return;

        if (element.isJsonArray()) {
            for (JsonElement child : element.getAsJsonArray()) {
                scanJsonForTextAndTranslations(child, relativePath, data, lang, config, stats);
            }
        } else if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();

            if (obj.has("translate") && obj.get("translate").isJsonPrimitive()) {
                String key = obj.get("translate").getAsString();
                if (!lang.has(key) && !data.getTranslations().has(key)) {
                    data.getTranslations().addProperty(key, key);
                    stats.setKeysFound(stats.getKeysFound() + 1);
                }
            }

            if (config.extractJsonRawStrings && obj.has("text") && obj.get("text").isJsonPrimitive()) {
                String text = obj.get("text").getAsString();
                if (isValidRawString(text, config.minRawStringLength, config.aggressiveRawStringFiltering)) {
                    JsonObject fileObj = data.getRawJson().has(relativePath) ? data.getRawJson().getAsJsonObject(relativePath) : new JsonObject();
                    if (!fileObj.has(text)) {
                        fileObj.addProperty(text, text);
                        data.getRawJson().add(relativePath, fileObj);
                        stats.setStringsFound(stats.getStringsFound() + 1);
                    }
                }
            }

            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                scanJsonForTextAndTranslations(entry.getValue(), relativePath, data, lang, config, stats);
            }
        }
    }

    private static void saveExportMap(Path baseDir, String targetLang, Map<String, ExportData> dataMap) {
        for (Map.Entry<String, ExportData> entry : dataMap.entrySet()) {
            String     id   = entry.getKey();
            ExportData data = entry.getValue();

            if (data.getTranslations().size() == 0 && data.getRaw().size() == 0 && data.getRawJson().size() == 0) continue;

            Path outDir = baseDir.resolve(id).resolve(targetLang);
            try {
                Files.createDirectories(outDir);

                if (data.getTranslations().size() > 0) {
                    try (Writer w = Files.newBufferedWriter(outDir.resolve("translations.json"))) {
                        GSON.toJson(data.getTranslations(), w);
                    }
                }
                if (data.getRaw().size() > 0) {
                    try (Writer w = Files.newBufferedWriter(outDir.resolve("raw.json"))) {
                        GSON.toJson(data.getRaw(), w);
                    }
                }
                if (data.getRawJson().size() > 0) {
                    try (Writer w = Files.newBufferedWriter(outDir.resolve("rawjson.json"))) {
                        GSON.toJson(data.getRawJson(), w);
                    }
                }
            } catch (Exception e) { Constants.LOGGER.error("Failed to save exports for {}", id, e); }
        }
    }

    private static JsonObject loadJsonStackFromResource(ResourceManager rm, ResourceLocation loc) {
        List<Resource> stack = rm.getResourceStack(loc);
        if (stack.isEmpty()) return null;

        JsonObject merged = new JsonObject();
        for (Resource res : stack) {
            try (Reader r = res.openAsReader()) {
                JsonObject obj = GSON.fromJson(r, JsonObject.class);
                if (obj != null) {
                    for (Map.Entry<String, JsonElement> entry : obj.entrySet()) merged.add(entry.getKey(), entry.getValue());
                }
            } catch (Exception ignored) { }
        }
        return merged.size() > 0 ? merged : null;
    }

    private static boolean isValidRawString(String s, int minLen, boolean aggressive) {
        if (s == null) return false;
        s = s.trim();
        if (s.length() < minLen) return false;

        if (s.startsWith(".")) return false;
        if (s.contains("/") || s.contains(":")) return false;

        if (s.endsWith(".png") || s.endsWith(".json") || s.endsWith(".class") ||
                s.endsWith(".mcmeta") || s.endsWith(".ogg") || s.endsWith(".zip") ||
                s.endsWith(".jar") || s.endsWith(".gz") || s.endsWith(".tar") ||
                s.endsWith(".nbt") || s.endsWith(".dat") || s.endsWith(".toml") ||
                s.endsWith(".cfg") || s.endsWith(".properties") || s.endsWith(".txt") ||
                s.endsWith(".md")) {
            return false;
        }

        if (s.matches("^(net|com|org|java|javax|io|sun|forge|minecraft)\\.[a-zA-Z0-9_.-]+$")) return false;

        if (s.matches("^[a-z0-9_\\-]+$")) return false;
        if (s.matches("^[a-zA-Z0-9_]+$")) return false;

        String lower = s.toLowerCase();
        if (lower.contains("must not be null") || lower.contains("cannot be null") ||
                lower.contains("can not be null") || lower.contains("should not be null") ||
                lower.contains("must not be empty") || lower.contains("cannot be empty") ||
                lower.contains("must be greater") || lower.contains("cannot continue") ||
                lower.contains("already registered") || lower.contains("not registered") ||
                lower.contains("unregistered") || lower.contains("must not be instantiated") ||
                lower.contains("returned null") || lower.contains("illegal format") ||
                lower.contains("should never happen")) {
            return false;
        }

        if (aggressive) {
            if (s.startsWith("^") || s.endsWith("$")) return false;
            if (s.contains("[a-z") || s.contains("[A-Z") || s.contains("[0-9") || s.contains("[^") || s.contains("(?")) return false;
            if (s.contains(".*") || s.contains(".+")) return false;

            if (s.contains("{}")) return false;

            if (s.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) return false;

            if (s.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) return false;

            if (s.matches("^[#0.,\\-]+$")) return false;
            if (s.contains("yyyy") || s.contains("HH.mm") || s.contains("MM-dd")) return false;

            if (s.contains("\\d") || s.contains("\\s") || s.contains("\\w") || s.contains("\\u")) return false;
            if (s.startsWith("[") && s.endsWith("]") && !s.contains(" ")) return false;
            if (s.matches("^%(\\d+\\$)?[\\d.]*[a-zA-Z]$")) return false;

            if (s.contains("{") && s.contains("}") && !s.contains(" ")) return false;
        }

        return !s.matches("^-?\\d+(\\.\\d+)?$");
    }

    public static void applyTranslations(String languageName) {
        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        loadTranslationsFromDir(baseDir, languageName);
    }

    public static void applyWorldTranslations(String languageName) {
        Path baseDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
        loadTranslationsFromDir(baseDir, languageName);
    }

    private static void loadTranslationsFromDir(Path baseDir, String languageName) {
        if (!Files.isDirectory(baseDir)) return;
        if (!(Language.getInstance() instanceof ClientLanguage clientLanguage)) return;

        ClientLanguageAccessor accessor   = (ClientLanguageAccessor) clientLanguage;
        Map<String, String>    newStorage = new HashMap<>(accessor.getStorage());

        try (Stream<Path> stream = Files.list(baseDir)) {
            stream.filter(Files::isDirectory).forEach(idDir -> {
                Path jsonFile = idDir.resolve(languageName).resolve("translations.json");
                if (Files.exists(jsonFile)) {
                    try (Reader reader = Files.newBufferedReader(jsonFile)) {
                        JsonObject loaded = GSON.fromJson(reader, JsonObject.class);
                        if (loaded != null) {
                            for (Map.Entry<String, JsonElement> entry : loaded.entrySet()) {
                                if (entry.getValue().isJsonPrimitive()) newStorage.put(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                    } catch (Exception ignored) { }
                }
            });
        } catch (Exception ignored) { }

        accessor.setStorage(newStorage);
    }
}