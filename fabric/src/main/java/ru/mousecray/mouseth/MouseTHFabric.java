package ru.mousecray.mouseth;

import net.fabricmc.api.ClientModInitializer;
import ru.mousecray.mouseth.config.ConfigManager;

public class MouseTHFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("MouseTH (Fabric) initialized!");
        ConfigManager.load();
    }
}