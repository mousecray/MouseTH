package ru.mousecray.mouseth.platform;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModFileInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ForgePlatformHelper implements IPlatformHelper {
    @Override public Path getGameDir()   { return FMLPaths.GAMEDIR.get(); }
    @Override public Path getConfigDir() { return FMLPaths.CONFIGDIR.get(); }

    @Override
    public Map<String, Path> getLoadedMods() {
        Map<String, Path> mods = new HashMap<>();
        for (IModFileInfo file : ModList.get().getModFiles()) {
            if (!file.getMods().isEmpty()) {
                String id = file.getMods().get(0).getModId();
                if (!id.equals("minecraft")) mods.put(id, file.getFile().findResource(""));
            }
        }
        return mods;
    }
}