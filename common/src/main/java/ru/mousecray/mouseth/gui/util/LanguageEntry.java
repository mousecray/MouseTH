package ru.mousecray.mouseth.gui.util;

public record LanguageEntry(String code, String name, String region) {
    public String displayName() {
        return name + " (" + region + ")";
    }
}
