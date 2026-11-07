package ru.mousecray.mouseth.gui.screen;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.gui.util.ModI18n;
import ru.mousecray.mouseth.gui.widget.CustomCheckbox;
import ru.mousecray.mouseth.gui.widget.HelpWidget;
import ru.mousecray.mouseth.patch.RawJsonPatcher;
import ru.mousecray.mouseth.platform.Services;
import ru.mousecray.mouseth.scanner.MouseTHScanner;
import ru.mousecray.mouseth.utils.ExportBackupHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class WorldExportSetupScreen extends Screen {
    private final Path     worldDir;
    private final Runnable onProceed;
    private       Button   exportBtn;
    private       boolean  isExporting = false;
    private       boolean  exported    = false;
    private       boolean  dontShowAgain;
    private       String   lastExportDate;

    public WorldExportSetupScreen(Path worldDir, Runnable onProceed) {
        super(Component.empty());
        this.worldDir = worldDir;
        this.onProceed = onProceed;
        dontShowAgain = !ConfigManager.get().showWorldSetupScreen;
        ModI18n.setLanguage(ConfigManager.get().targetLanguage);
    }

    @Override
    protected void init() {
        super.init();
        MouseTHConfig config = ConfigManager.get();

        Path worldMdataDir = worldDir.resolve("mdata");
        lastExportDate = ExportBackupHelper.getLastExportTime(worldMdataDir, "last_export.txt");

        int centerX = width / 2;
        int startY  = height / 2 - 35;

        CustomCheckbox loadWorldCb = new CustomCheckbox(
                centerX - 110, startY, 200, 20,
                ModI18n.get("mouseth.gui.config.load_world_datapacks"), config.loadWorldDatapacks,
                val -> {
                    config.loadWorldDatapacks = val;
                    ConfigManager.save();
                }
        );
        addRenderableWidget(loadWorldCb);
        addRenderableWidget(new HelpWidget(centerX + 96, startY + 3, ModI18n.get("mouseth.gui.config.load_world_datapacks.desc")));

        CustomCheckbox dontShowCb = new CustomCheckbox(
                centerX - 110, startY + 25, 200, 20,
                ModI18n.get("mouseth.gui.world.dont_show"), dontShowAgain,
                val -> {
                    dontShowAgain = val;
                    config.showWorldSetupScreen = !val;
                    ConfigManager.save();
                }
        );
        addRenderableWidget(dontShowCb);
        addRenderableWidget(new HelpWidget(centerX + 96, startY + 28, ModI18n.get("mouseth.gui.world.dont_show.desc")));

        exportBtn = Button.builder(ModI18n.get("mouseth.gui.world.btn_export"), btn -> {
            Path rootMouseTH  = Services.PLATFORM.getGameDir().resolve("MouseTH");
            Path rootMdataDir = Services.PLATFORM.getGameDir().resolve("mdata");

            if (ExportBackupHelper.hasExistingDatapackData(worldDir, rootMouseTH)) {
                BooleanConsumer callback = confirmed -> {
                    minecraft.setScreen(this);
                    if (confirmed) startWorldExtraction(rootMouseTH, rootMdataDir, worldMdataDir, config);
                };
                minecraft.setScreen(new ConfirmScreen(
                        callback,
                        ModI18n.get("mouseth.gui.confirm.title"),
                        ModI18n.get("mouseth.gui.confirm.desc"),
                        ModI18n.get("mouseth.gui.confirm.yes"),
                        ModI18n.get("mouseth.gui.confirm.no")
                ));
            } else startWorldExtraction(rootMouseTH, rootMdataDir, worldMdataDir, config);
        }).bounds(centerX - 110, startY + 60, 105, 20).build();
        addRenderableWidget(exportBtn);

        addRenderableWidget(Button.builder(ModI18n.get("mouseth.gui.world.btn_continue"), btn -> {
            ConfigManager.save();
            if (config.loadWorldDatapacks) {
                MouseTHScanner.applyWorldTranslations(config.targetLanguage);
                if (config.translateJsonRawStrings) RawJsonPatcher.loadWorldTranslations(config.targetLanguage);
            }
            if (onProceed != null) onProceed.run();
            else minecraft.setScreen(null);
        }).bounds(centerX + 5, startY + 60, 105, 20).build());
    }

    private void startWorldExtraction(Path rootMouseTH, Path rootMdataDir, Path worldMdataDir, MouseTHConfig config) {
        isExporting = true;
        exportBtn.active = false;
        CompletableFuture.runAsync(() -> {
            ExportBackupHelper.createBackup(rootMouseTH, rootMdataDir);
            MouseTHScanner.runWorldExtraction(worldDir, config.targetLanguage);
            ExportBackupHelper.saveLastExportTime(worldMdataDir, "last_export.txt");
        }).thenRun(() -> minecraft.execute(() -> {
            isExporting = false;
            exported = true;
            exportBtn.active = true;
            lastExportDate = ExportBackupHelper.getLastExportTime(worldMdataDir, "last_export.txt");
        }));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.world.title"), width / 2, height / 2 - 70, 0xFFFFFF);

        String dateText = lastExportDate != null
                ? ModI18n.get("mouseth.gui.export.last_date").getString() + ": " + lastExportDate
                : ModI18n.get("mouseth.gui.export.last_date.never").getString();
        guiGraphics.drawCenteredString(font, dateText, width / 2, height / 2 - 54, 0x888888);

        if (isExporting) {
            guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.world.exporting"), width / 2, height / 2 + 95, 0xFFFF00);
        } else if (exported) {
            guiGraphics.drawCenteredString(font, ModI18n.get("mouseth.gui.world.export_success"), width / 2, height / 2 + 95, 0x00FF00);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}