package ru.mousecray.mouseth.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.gui.screen.LanguageSelectorScreen;
import ru.mousecray.mouseth.gui.screen.MouseTHExportSetupScreen;
import ru.mousecray.mouseth.gui.screen.WorldExportSetupScreen;
import ru.mousecray.mouseth.patch.RawJsonPatcher;
import ru.mousecray.mouseth.scanner.MouseTHScanner;

import java.nio.file.Path;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Unique private static boolean mouseTH$hasDumpedInitial      = false;
    @Unique private static String  mouseTH$lastProcessedWorldKey = null;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;

        if (screen instanceof TitleScreen && !mouseTH$hasDumpedInitial) {
            mouseTH$hasDumpedInitial = true;
            MouseTHConfig config = ConfigManager.get();

            if (!config.loadMode) {
                if (config.showLanguageSelector) minecraft.setScreen(new LanguageSelectorScreen());
                else {
                    String currentSelected = minecraft.getLanguageManager().getSelected();
                    if (!currentSelected.equals(config.targetLanguage)) {
                        minecraft.getLanguageManager().setSelected(config.targetLanguage);
                        minecraft.options.languageCode = config.targetLanguage;
                        minecraft.options.save();
                        minecraft.reloadResourcePacks();
                    }
                    minecraft.setScreen(new MouseTHExportSetupScreen());
                }
                ci.cancel();
                return;
            }
        }

        if (screen == null && minecraft.level != null) {
            IntegratedServer server = minecraft.getSingleplayerServer();
            if (server != null) {
                Path   worldDir = server.getWorldPath(LevelResource.ROOT);
                String worldKey = worldDir.toAbsolutePath().normalize().toString();

                if (!worldKey.equals(mouseTH$lastProcessedWorldKey)) {
                    mouseTH$lastProcessedWorldKey = worldKey;
                    MouseTHConfig config = ConfigManager.get();

                    if (config.loadMode) {
                        if (config.loadWorldDatapacks) {
                            MouseTHScanner.applyWorldTranslations(config.targetLanguage);
                            if (config.translateJsonRawStrings) RawJsonPatcher.loadWorldTranslations(config.targetLanguage);
                        }
                    } else if (config.showWorldSetupScreen) {
                        minecraft.setScreen(new WorldExportSetupScreen(worldDir, () -> minecraft.setScreen(null)));
                        ci.cancel();
                    }
                }
            }
        } else if (minecraft.level == null && minecraft.getSingleplayerServer() == null) mouseTH$lastProcessedWorldKey = null;
    }
}