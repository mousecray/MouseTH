package ru.mousecray.mouseth.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class CustomCheckbox extends AbstractButton {
    private       boolean           selected;
    private final Consumer<Boolean> onValueChange;

    public CustomCheckbox(int x, int y, int width, int height, Component message, boolean initialValue, Consumer<Boolean> onValueChange) {
        super(x, y, width, height, message);
        selected = initialValue;
        this.onValueChange = onValueChange;
    }

    @Override
    public void onPress() {
        if (!active) return;
        selected = !selected;
        if (onValueChange != null) {
            onValueChange.accept(selected);
        }
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int boxSize = 14;
        int boxY    = getY() + (getHeight() - boxSize) / 2;

        guiGraphics.fill(getX(), boxY, getX() + boxSize, boxY + boxSize, 0xFF000000);

        int borderColor = !active ? 0xFF555555 : (isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFAAAAAA);
        guiGraphics.renderOutline(getX(), boxY, boxSize, boxSize, borderColor);

        if (selected) {
            int checkColor = !active ? 0xFF557755 : 0xFF55FF55;
            guiGraphics.fill(getX() + 3, boxY + 3, getX() + boxSize - 3, boxY + boxSize - 3, checkColor);
        }

        int textColor = !active ? 0xFF777777 : 0xFFFFFF;
        guiGraphics.drawString(Minecraft.getInstance().font, getMessage(), getX() + boxSize + 6, getY() + (getHeight() - 8) / 2, textColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}