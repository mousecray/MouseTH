package ru.mousecray.mouseth.gui.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ModI18n {
    private static final Map<String, String> TRANSLATIONS = new HashMap<>();

    public static void setLanguage(String langCode) {
        TRANSLATIONS.clear();
        loadLang("en_us");
        if (langCode != null && !langCode.equalsIgnoreCase("en_us")) {
            loadLang(langCode.toLowerCase());
        }
    }

    private static void loadLang(String code) {
        try (InputStream is = ModI18n.class.getResourceAsStream("/assets/mouseth/lang/" + code + ".json")) {
            if (is != null) {
                try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                    if (obj != null) {
                        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                            if (entry.getValue().isJsonPrimitive()) {
                                TRANSLATIONS.put(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
    }

    public static Component get(String key) {
        String val = TRANSLATIONS.get(key);
        return val != null ? Component.literal(val) : Component.translatable(key);
    }
}