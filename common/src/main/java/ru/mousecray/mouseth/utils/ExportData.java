package ru.mousecray.mouseth.utils;

import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class ExportData {
    private final JsonObject  translations = new JsonObject();
    private final JsonObject  raw          = new JsonObject();
    private final JsonObject  rawJson      = new JsonObject();
    private final Set<String> knownKeys    = new HashSet<>();

    public JsonObject getTranslations() { return translations; }
    public JsonObject getRaw()          { return raw; }
    public JsonObject getRawJson()      { return rawJson; }
    public Set<String> getKnownKeys()   { return knownKeys; }
}
