package ru.mousecray.mouseth.config;

public class MouseTHConfig {
    public boolean extractMissingTranslations   = true;
    public boolean extractMissingKeys           = true;
    public boolean extractRawStrings            = true;
    public int     minRawStringLength           = 3;
    public boolean aggressiveRawStringFiltering = true;
    public boolean extractJsonRawStrings        = true;
    public boolean scanResourcePacks            = false;

    public boolean loadMode                = false;
    public boolean showLanguageSelector    = true;
    public boolean showWorldSetupScreen    = true;
    public boolean translateKeys           = true;
    public boolean translateRawStrings     = false;
    public boolean bindToLineNumbers       = true;
    public boolean translateJsonRawStrings = true;
    public boolean loadResourcePacks       = false;
    public boolean loadWorldDatapacks      = true;
    public String  targetLanguage          = "en_us";
}