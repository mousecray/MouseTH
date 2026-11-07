package ru.mousecray.mouseth.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.network.chat.Component;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.gui.util.LanguageEntry;
import ru.mousecray.mouseth.gui.util.ModI18n;
import ru.mousecray.mouseth.gui.widget.CustomCheckbox;
import ru.mousecray.mouseth.gui.widget.DropdownWidget;
import ru.mousecray.mouseth.gui.widget.HelpWidget;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class LanguageSelectorScreen extends Screen {
    private DropdownWidget<LanguageEntry> dropdown;
    private CustomCheckbox                dontShowAgainCheckbox;
    private String                        selectedLanguageCode;
    private String                        userChosenLanguageCode;
    private LanguageEntry                 selectedEntry;
    private boolean                       dontShowAgain;

    public LanguageSelectorScreen() {
        super(Component.empty());
        selectedLanguageCode = ConfigManager.get().targetLanguage;
        dontShowAgain = !ConfigManager.get().showLanguageSelector;
        ModI18n.setLanguage(selectedLanguageCode);
    }

    @Override
    protected void init() {
        super.init();

        Map<String, LanguageInfo> languages    = minecraft.getLanguageManager().getLanguages();
        List<LanguageEntry>       languageList = new ArrayList<>();

        for (Map.Entry<String, LanguageInfo> entry : languages.entrySet()) {
            LanguageInfo info = entry.getValue();
            languageList.add(new LanguageEntry(entry.getKey(), info.name(), info.region()));
        }
        languageList.sort(Comparator.comparing(LanguageEntry::name));

        String desiredCode      = userChosenLanguageCode != null ? userChosenLanguageCode : ConfigManager.get().targetLanguage;
        String targetNormalized = normalizeCode(desiredCode);

        LanguageEntry matchedLang = null;

        for (LanguageEntry entry : languageList) {
            if (normalizeCode(entry.code()).equals(targetNormalized)) {
                matchedLang = entry;
                break;
            }
        }

        if (matchedLang == null) {
            for (LanguageEntry entry : languageList) {
                String clean = normalizeCode(entry.code());
                if (clean.startsWith(targetNormalized) || targetNormalized.startsWith(clean)) {
                    matchedLang = entry;
                    break;
                }
            }
        }

        if (matchedLang == null && targetNormalized.startsWith("ru")) {
            for (LanguageEntry entry : languageList) {
                String lowerName = entry.name().toLowerCase();
                if (lowerName.contains("рус") || lowerName.contains("russ")) {
                    matchedLang = entry;
                    break;
                }
            }
        }

        if (matchedLang == null && userChosenLanguageCode == null) {
            String mcSelected = normalizeCode(minecraft.getLanguageManager().getSelected());
            for (LanguageEntry entry : languageList) {
                if (normalizeCode(entry.code()).equals(mcSelected)) {
                    matchedLang = entry;
                    break;
                }
            }
        }

        if (matchedLang == null && !languageList.isEmpty()) {
            matchedLang = languageList.get(0);
        }

        selectedEntry = matchedLang;

        if (selectedEntry != null) {
            selectedLanguageCode = selectedEntry.code();
            ModI18n.setLanguage(selectedLanguageCode);
        }

        int centerX = width / 2;
        int startY  = height / 2 - 30;

        dropdown = new DropdownWidget<>(centerX - 100, startY, 200, 20, languageList, selectedEntry,
                entry -> Component.literal(entry.displayName()),
                chosen -> {
                    userChosenLanguageCode = chosen.code();
                    selectedEntry = chosen;
                    selectedLanguageCode = chosen.code();
                    ModI18n.setLanguage(selectedLanguageCode);
                    rebuildWidgets();
                }
        );
        addRenderableWidget(dropdown);

        dontShowAgainCheckbox = new CustomCheckbox(centerX - 100, startY + 30, 180, 20,
                ModI18n.get("mouseth.gui.lang_selector.dont_show"),
                dontShowAgain,
                val -> dontShowAgain = val);
        addRenderableWidget(dontShowAgainCheckbox);

        addRenderableWidget(new HelpWidget(centerX + 86, startY + 33, ModI18n.get("mouseth.gui.lang_selector.dont_show.desc")));

        addRenderableWidget(Button.builder(ModI18n.get("mouseth.gui.lang_selector.continue"), btn -> {
            MouseTHConfig config = ConfigManager.get();
            config.targetLanguage = selectedLanguageCode;
            config.showLanguageSelector = !dontShowAgain;
            ConfigManager.save();

            String currentSelected = minecraft.getLanguageManager().getSelected();
            if (!currentSelected.equalsIgnoreCase(selectedLanguageCode)) {
                minecraft.getLanguageManager().setSelected(selectedLanguageCode);
                minecraft.options.languageCode = selectedLanguageCode;
                minecraft.options.save();
                minecraft.reloadResourcePacks();
            }

            minecraft.setScreen(new MouseTHExportSetupScreen());
        }).bounds(centerX - 100, startY + 65, 200, 20).build());
    }

    private static String normalizeCode(String code) {
        if (code == null) return "";
        String s = code.trim().toLowerCase();
        if (s.contains(":")) s = s.substring(s.indexOf(':') + 1);
        if (s.endsWith(".json")) s = s.substring(0, s.length() - 5);
        if (s.startsWith("lang/")) s = s.substring(5);
        return s.replace('-', '_');
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (dropdown != null && dropdown.isOpen()) {
            if (dropdown.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (dropdown != null && dropdown.isOpen()) {
            if (dropdown.mouseScrolled(mouseX, mouseY, delta)) return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.lang_selector.title"), width / 2, height / 2 - 65, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (dropdown != null) dropdown.renderDropdownList(guiGraphics, mouseX, mouseY);
    }
}