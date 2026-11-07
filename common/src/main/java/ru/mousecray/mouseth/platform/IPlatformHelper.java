package ru.mousecray.mouseth.platform;

import java.nio.file.Path;
import java.util.Map;

public interface IPlatformHelper {
    Path getGameDir();
    Path getConfigDir();
    Map<String, Path> getLoadedMods();
}