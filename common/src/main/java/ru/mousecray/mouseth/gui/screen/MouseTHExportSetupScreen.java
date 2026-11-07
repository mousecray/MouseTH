package ru.mousecray.mouseth.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import ru.mousecray.mouseth.MouseTH;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.gui.components.OptionsScrollList;
import ru.mousecray.mouseth.gui.util.ModI18n;
import ru.mousecray.mouseth.gui.widget.CustomCheckbox;
import ru.mousecray.mouseth.gui.widget.HelpWidget;
import ru.mousecray.mouseth.patch.RawJsonPatcher;
import ru.mousecray.mouseth.platform.Services;
import ru.mousecray.mouseth.scanner.MouseTHScanner;
import ru.mousecray.mouseth.utils.ExportBackupHelper;
import ru.mousecray.mouseth.utils.Stats;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class MouseTHExportSetupScreen extends Screen {
    private OptionsScrollList scrollList;
    private Button            exportBtn;
    private Stats             stats;
    private boolean           isExporting = false;
    private String            lastExportDate;

    public MouseTHExportSetupScreen() {
        super(Component.empty());
        ModI18n.setLanguage(ConfigManager.get().targetLanguage);
    }

    @Override
    protected void init() {
        super.init();
        MouseTHConfig config = ConfigManager.get();

        Path mdataDir = Services.PLATFORM.getGameDir().resolve("mdata");
        lastExportDate = ExportBackupHelper.getLastExportTime(mdataDir, "last_export.txt");

        scrollList = new OptionsScrollList(minecraft, width, height - 70, 42, 30);
        addWidget(scrollList);

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

        Button openFolderBtn = Button.builder(ModI18n.get("mouseth.gui.extract.open_folder"), btn -> {
            Path outDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
            Util.getPlatform().openUri(outDir.toUri());
        }).bounds(0, 0, 100, 20).build();
        scrollList.addOption(openFolderBtn);

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

        int btnWidth   = 110;
        int gap        = 8;
        int totalWidth = btnWidth * 3 + gap * 2;
        int startX     = width / 2 - totalWidth / 2;
        int bottomY    = height - 26;

        exportBtn = Button.builder(ModI18n.get("mouseth.gui.setup.btn_export"), btn -> {
            Path targetDir = Services.PLATFORM.getGameDir().resolve("MouseTH");
            if (ExportBackupHelper.hasExistingData(targetDir)) {
                BooleanConsumer callback = confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) startExtraction(targetDir, mdataDir, config);
                };
                minecraft.setScreen(new ConfirmScreen(
                        callback,
                        ModI18n.get("mouseth.gui.confirm.title"),
                        ModI18n.get("mouseth.gui.confirm.desc"),
                        ModI18n.get("mouseth.gui.confirm.yes"),
                        ModI18n.get("mouseth.gui.confirm.no")
                ));
            } else startExtraction(targetDir, mdataDir, config);
        }).bounds(startX, bottomY, btnWidth, 20).build();
        addRenderableWidget(exportBtn);

        addRenderableWidget(Button.builder(ModI18n.get("mouseth.gui.setup.btn_load_game"), btn -> {
            ConfigManager.save();
            MouseTH.enableForSession();
            if (config.translateKeys) MouseTHScanner.applyTranslations(config.targetLanguage);
            if (config.translateJsonRawStrings) RawJsonPatcher.enableForSession(config.targetLanguage, config.loadResourcePacks);
            minecraft.setScreen(new TitleScreen(false));
        }).bounds(startX + btnWidth + gap, bottomY, btnWidth, 20).build());

        addRenderableWidget(Button.builder(ModI18n.get("mouseth.gui.setup.btn_skip_load"), btn -> {
            ConfigManager.save();
            minecraft.setScreen(new TitleScreen(false));
        }).bounds(startX + (btnWidth + gap) * 2, bottomY, btnWidth, 20).build());
    }

    private void startExtraction(Path targetDir, Path mdataDir, MouseTHConfig config) {
        isExporting = true;
        exportBtn.active = false;
        CompletableFuture.supplyAsync(() -> {
            ExportBackupHelper.createBackup(targetDir, mdataDir);
            Stats result = MouseTHScanner.runExtraction(config.targetLanguage);
            ExportBackupHelper.saveLastExportTime(mdataDir, "last_export.txt");
            return result;
        }).thenAccept(result -> {
            stats = result;
            isExporting = false;
            lastExportDate = ExportBackupHelper.getLastExportTime(mdataDir, "last_export.txt");
            minecraft.execute(() -> exportBtn.active = true);
        });
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        if (scrollList != null) scrollList.render(guiGraphics, mouseX, mouseY, partialTick);

        String dateText = lastExportDate != null
                ? ModI18n.get("mouseth.gui.export.last_date").getString() + ": " + lastExportDate
                : ModI18n.get("mouseth.gui.export.last_date.never").getString();
        guiGraphics.drawCenteredString(font, dateText, width / 2, 24, 0x888888);

        if (isExporting) {
            guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.extract.in_progress"), width / 2, 10, 0xFFFF00);
        } else if (stats != null) {
            guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.extract.title"), width / 2, 6, 0x00FF00);
            String keysS = ModI18n.get("mouseth.gui.extract.keys_found").getString() + ": " + stats.getKeysFound();
            String rawS  = ModI18n.get("mouseth.gui.extract.strings_found").getString() + ": " + stats.getStringsFound();
            guiGraphics.drawString(font, keysS, 10, 24, 0xFFFFFF);
            guiGraphics.drawString(font, rawS, width - font.width(rawS) - 10, 24, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.setup.title"), width / 2, 10, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}