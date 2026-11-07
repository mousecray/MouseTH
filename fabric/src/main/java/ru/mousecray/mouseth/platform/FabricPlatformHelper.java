package ru.mousecray.mouseth.platform;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FabricPlatformHelper implements IPlatformHelper {
    @Override public Path getGameDir()   { return FabricLoader.getInstance().getGameDir(); }
    @Override public Path getConfigDir() { return FabricLoader.getInstance().getConfigDir(); }

    @Override
    public Map<String, Path> getLoadedMods() {
        Map<String, Path> mods = new HashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId();
            if (!id.equals("minecraft")) mods.put(id, mod.getRootPaths().get(0));
        }
        return mods;
    }
}