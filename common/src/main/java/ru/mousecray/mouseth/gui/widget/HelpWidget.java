package ru.mousecray.mouseth.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;


public class HelpWidget extends AbstractWidget {

    public HelpWidget(int x, int y, Component tooltip) {
        super(x, y, 14, 14, Component.literal("?"));
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int color = isHovered() ? 0xFFFFAA00 : 0xFF888888;
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0x88000000);
        guiGraphics.renderOutline(getX(), getY(), width, height, color);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "?", getX() + width / 2, getY() + (height - 8) / 2, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(false);
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}