package ru.mousecray.mouseth.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.mousecray.mouseth.MouseTH;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.config.MouseTHConfig;
import ru.mousecray.mouseth.patch.RawJsonPatcher;
import ru.mousecray.mouseth.platform.Services;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {
    @Inject(method = "loadFrom", at = @At("RETURN"))
    private static void onLoadFrom(ResourceManager resourceManager, List<String> languages, boolean defaultToEnUs, CallbackInfoReturnable<ClientLanguage> cir) {
        MouseTHConfig config = ConfigManager.get();
        if (!config.loadMode && !MouseTH.isSessionActive()) return;
        if (config.translateJsonRawStrings) RawJsonPatcher.init();

        if (config.translateKeys) {
            ClientLanguage         originalLanguage = cir.getReturnValue();
            ClientLanguageAccessor accessor         = (ClientLanguageAccessor) originalLanguage;
            Map<String, String>    newStorage       = new HashMap<>(accessor.getStorage());
            Path                   baseDir          = Services.PLATFORM.getGameDir().resolve("MouseTH");
            Map<String, Path>      loadedMods       = Services.PLATFORM.getLoadedMods();

            if (!Files.isDirectory(baseDir)) return;

            for (String languageName : languages) {
                try (Stream<Path> stream = Files.list(baseDir)) {
                    stream.filter(Files::isDirectory).forEach(idDir -> {
                        String id = idDir.getFileName().toString();
                        if (
                                !loadedMods.containsKey(id) && !config.loadResourcePacks
                                        && !config.loadWorldDatapacks && !id.equals("minecraft")
                        ) return;

                        Path jsonFile = idDir.resolve(languageName).resolve("translations.json");
                        if (Files.exists(jsonFile)) {
                            try (Reader reader = Files.newBufferedReader(jsonFile)) {
                                JsonObject loaded = new Gson().fromJson(reader, JsonObject.class);
                                if (loaded != null) {
                                    for (Map.Entry<String, JsonElement> entry : loaded.entrySet()) {
                                        if (entry.getValue().isJsonPrimitive()) {
                                            newStorage.put(entry.getKey(), entry.getValue().getAsString());
                                        }
                                    }
                                }
                            } catch (Exception ignored) { }
                        }
                    });
                } catch (Exception ignored) { }
            }

            accessor.setStorage(newStorage);
        }
    }
}