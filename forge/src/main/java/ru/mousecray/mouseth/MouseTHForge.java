package ru.mousecray.mouseth;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import ru.mousecray.mouseth.config.ConfigManager;
import ru.mousecray.mouseth.gui.screen.MouseTHConfigScreen;

@Mod(Constants.MOD_ID)
public class MouseTHForge {
    public MouseTHForge(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        context.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> new MouseTHConfigScreen(screen))
        );

        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Constants.LOGGER.info("MouseTH (Forge) initialized!");
        ConfigManager.load();
    }
}