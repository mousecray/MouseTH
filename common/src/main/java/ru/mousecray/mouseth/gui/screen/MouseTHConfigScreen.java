package ru.mousecray.mouseth.gui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.gui.components.OptionsScrollList;
import ru.mousecray.mouseth.gui.util.ModI18n;
import ru.mousecray.mouseth.gui.widget.CustomCheckbox;
import ru.mousecray.mouseth.gui.widget.HelpWidget;


public class MouseTHConfigScreen extends Screen {
    private final Screen            previousScreen;
    private       OptionsScrollList scrollList;

    public MouseTHConfigScreen(Screen previousScreen) {
        super(Component.empty());
        this.previousScreen = previousScreen;
        ModI18n.setLanguage(ConfigManager.get().targetLanguage);
    }

    @Override
    protected void init() {
        super.init();
        MouseTHConfig config = ConfigManager.get();

        scrollList = new OptionsScrollList(minecraft, width, height - 70, 35, 30);
        addWidget(scrollList);

        scrollList.addHeader(ModI18n.get("mouseth.gui.config.general_col"), 0xAAAAAA);

        CustomCheckbox showSelectorCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.show_selector"), config.showLanguageSelector,
                val -> {
                    config.showLanguageSelector = val;
                    ConfigManager.save();
                }
        );
        HelpWidget showSelectorHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.show_selector.desc"));
        scrollList.addOption(showSelectorCb, showSelectorHelp);

        CustomCheckbox showWorldSetupCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.show_world_selector"), config.showWorldSetupScreen,
                val -> {
                    config.showWorldSetupScreen = val;
                    ConfigManager.save();
                }
        );
        HelpWidget showWorldSetupHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.show_world_selector.desc"));
        scrollList.addOption(showWorldSetupCb, showWorldSetupHelp);

        EditBox languageBox = new EditBox(font, 0, 0, 70, 20, Component.empty());
        languageBox.setValue(config.targetLanguage);
        languageBox.setResponder(val -> {
            config.targetLanguage = val.trim().toLowerCase();
            ConfigManager.save();
        });
        HelpWidget languageHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.target_lang.desc"));
        scrollList.addOptionWithLabel(ModI18n.get("mouseth.gui.config.target_lang"), languageBox, languageHelp);

        scrollList.addHeader(ModI18n.get("mouseth.gui.setup.export_col"), 0x55FF55);

        CustomCheckbox extractTransCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.extract_translations"), config.extractMissingTranslations,
                val -> {
                    config.extractMissingTranslations = val;
                    ConfigManager.save();
                }
        );
        HelpWidget extractTransHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.extract_translations.desc"));
        scrollList.addOption(extractTransCb, extractTransHelp);

        CustomCheckbox extractKeysCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.extract_keys"), config.extractMissingKeys,
                val -> {
                    config.extractMissingKeys = val;
                    ConfigManager.save();
                }
        );
        HelpWidget extractKeysHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.extract_keys.desc"));
        scrollList.addOption(extractKeysCb, extractKeysHelp);

        CustomCheckbox extractHardcodedCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.extract_hardcoded"), config.extractRawStrings,
                val -> {
                    config.extractRawStrings = val;
                    ConfigManager.save();
                }
        );
        HelpWidget extractHardcodedHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.extract_hardcoded.desc"));
        scrollList.addOption(extractHardcodedCb, extractHardcodedHelp);

        CustomCheckbox aggressiveFilteringCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.aggressive_raw_filtering"), config.aggressiveRawStringFiltering,
                val -> {
                    config.aggressiveRawStringFiltering = val;
                    ConfigManager.save();
                }
        );
        HelpWidget aggressiveFilteringHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.aggressive_raw_filtering.desc"));
        scrollList.addOption(aggressiveFilteringCb, aggressiveFilteringHelp);

        EditBox minLengthBox = new EditBox(font, 0, 0, 50, 20, Component.empty());
        minLengthBox.setValue(String.valueOf(config.minRawStringLength));
        minLengthBox.setResponder(val -> {
            try {
                config.minRawStringLength = Integer.parseInt(val);
                ConfigManager.save();
            } catch (NumberFormatException ignored) { }
        });
        HelpWidget minLengthHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.extract.min_length.desc"));
        scrollList.addOptionWithLabel(ModI18n.get("mouseth.gui.extract.min_length"), minLengthBox, minLengthHelp);

        CustomCheckbox extractJsonRawCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.extract_json_raw"), config.extractJsonRawStrings,
                val -> {
                    config.extractJsonRawStrings = val;
                    ConfigManager.save();
                }
        );
        HelpWidget extractJsonRawHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.extract_json_raw.desc"));
        scrollList.addOption(extractJsonRawCb, extractJsonRawHelp);

        CustomCheckbox scanResourcePacksCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.scan_resourcepacks"), config.scanResourcePacks,
                val -> {
                    config.scanResourcePacks = val;
                    ConfigManager.save();
                }
        );
        HelpWidget scanResourcePacksHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.scan_resourcepacks.desc"));
        scrollList.addOption(scanResourcePacksCb, scanResourcePacksHelp);

        scrollList.addHeader(ModI18n.get("mouseth.gui.setup.load_col"), 0xFFAA00);

        CustomCheckbox loadModeCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.load_mode"), config.loadMode,
                val -> {
                    config.loadMode = val;
                    ConfigManager.save();
                }
        );
        HelpWidget loadModeHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.load_mode.desc"));
        scrollList.addOption(loadModeCb, loadModeHelp);

        CustomCheckbox translateKeysCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.translate_keys"), config.translateKeys,
                val -> {
                    config.translateKeys = val;
                    ConfigManager.save();
                }
        );
        HelpWidget translateKeysHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.translate_keys.desc"));
        scrollList.addOption(translateKeysCb, translateKeysHelp);

        CustomCheckbox translateRawCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.translate_raw"), config.translateRawStrings,
                val -> {
                    config.translateRawStrings = val;
                    ConfigManager.save();
                }
        );
        HelpWidget translateRawHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.translate_raw.desc"));
        scrollList.addOptionWithSubNote(translateRawCb, ModI18n.get("mouseth.gui.config.translate_raw.note"), translateRawHelp);

        CustomCheckbox bindLineNumbersCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.bind_line_numbers"), config.bindToLineNumbers,
                val -> {
                    config.bindToLineNumbers = val;
                    ConfigManager.save();
                }
        );
        HelpWidget bindLineNumbersHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.bind_line_numbers.desc"));
        scrollList.addOption(bindLineNumbersCb, bindLineNumbersHelp);

        CustomCheckbox translateJsonRawCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.translate_json_raw"), config.translateJsonRawStrings,
                val -> {
                    config.translateJsonRawStrings = val;
                    ConfigManager.save();
                }
        );
        HelpWidget translateJsonRawHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.translate_json_raw.desc"));
        scrollList.addOption(translateJsonRawCb, translateJsonRawHelp);

        CustomCheckbox loadResourcePacksCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.load_resourcepacks"), config.loadResourcePacks,
                val -> {
                    config.loadResourcePacks = val;
                    ConfigManager.save();
                }
        );
        HelpWidget loadResourcePacksHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.load_resourcepacks.desc"));
        scrollList.addOption(loadResourcePacksCb, loadResourcePacksHelp);

        CustomCheckbox loadWorldDatapacksCb = new CustomCheckbox(
                0, 0, 100, 20,
                ModI18n.get("mouseth.gui.config.load_world_datapacks"), config.loadWorldDatapacks,
                val -> {
                    config.loadWorldDatapacks = val;
                    ConfigManager.save();
                }
        );
        HelpWidget loadWorldDatapacksHelp = new HelpWidget(0, 0, ModI18n.get("mouseth.gui.config.load_world_datapacks.desc"));
        scrollList.addOption(loadWorldDatapacksCb, loadWorldDatapacksHelp);

        addRenderableWidget(Button.builder(ModI18n.get("mouseth.gui.config.save_exit"), btn -> {
            ConfigManager.save();
            minecraft.setScreen(previousScreen);
        }).bounds(width / 2 - 100, height - 26, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        if (scrollList != null) {
            scrollList.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.setup.title"), width / 2, 12, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}