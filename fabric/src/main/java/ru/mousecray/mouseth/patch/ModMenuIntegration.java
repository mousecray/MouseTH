package ru.mousecray.mouseth.patch;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.mousecray.mouseth.gui.screen.MouseTHConfigScreen;

public class ModMenuIntegration implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() { return MouseTHConfigScreen::new; }
}