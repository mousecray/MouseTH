package ru.mousecray.mouseth.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownWidget<T> extends AbstractWidget {
    private final List<T>                items;
    private final Function<T, Component> labelExtractor;
    private final Consumer<T>            onSelect;
    private       T                      selectedItem;
    private       boolean                open                = false;
    private       int                    scrollOffset        = 0;
    private final int                    itemHeight          = 18;
    private final int                    maxVisibleItems     = 7;
    private       boolean                isDraggingScrollbar = false;

    public DropdownWidget(int x, int y, int width, int height, List<T> items, T defaultItem, Function<T, Component> labelExtractor, Consumer<T> onSelect) {
        super(x, y, width, height, Component.empty());
        this.items = items;
        selectedItem = defaultItem;
        this.labelExtractor = labelExtractor;
        this.onSelect = onSelect;
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T item) {
        selectedItem = item;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) return false;
        if (mouseX >= getX() && mouseX <= getX() + width) {
            if (mouseY >= getY() && mouseY <= getY() + height) {
                return true;
            }
            if (open) {
                int visibleCount = Math.min(items.size(), maxVisibleItems);
                int listHeight   = visibleCount * itemHeight;
                int listY        = getY() + height + 2;
                return mouseY >= listY && mouseY <= listY + listHeight;
            }
        }
        return false;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF000000);
        guiGraphics.renderOutline(getX(), getY(), width, height, isHoveredOrFocused() ? 0xFFFFFFFF : 0xFFAAAAAA);

        Component text = selectedItem != null ? labelExtractor.apply(selectedItem) : Component.literal("-");
        guiGraphics.drawString(Minecraft.getInstance().font, text, getX() + 6, getY() + (height - 8) / 2, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font, open ? "▲" : "▼", getX() + width - 12, getY() + (height - 8) / 2, 0xAAAAAA);
    }

    public void renderDropdownList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!open) return;

        int     visibleCount = Math.min(items.size(), maxVisibleItems);
        int     listHeight   = visibleCount * itemHeight;
        int     listY        = getY() + height + 2;
        boolean hasScrollbar = items.size() > maxVisibleItems;
        int     contentWidth = hasScrollbar ? width - 8 : width;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 400);

        guiGraphics.fill(getX(), listY, getX() + width, listY + listHeight, 0xFA101010);
        guiGraphics.renderOutline(getX(), listY, width, listHeight, 0xFFFFFFFF);

        for (int i = 0; i < visibleCount; i++) {
            int itemIndex = i + scrollOffset;
            if (itemIndex >= items.size()) break;

            T   item         = items.get(itemIndex);
            int currentItemY = listY + i * itemHeight;

            boolean hovered = mouseX >= getX() && mouseX <= getX() + contentWidth && mouseY >= currentItemY && mouseY < currentItemY + itemHeight;
            if (hovered) {
                guiGraphics.fill(getX() + 1, currentItemY, getX() + contentWidth - 1, currentItemY + itemHeight, 0xFF353535);
            }

            int textColor = item.equals(selectedItem) ? 0xFF55FF55 : 0xFFFFFFFF;
            guiGraphics.drawString(Minecraft.getInstance().font, labelExtractor.apply(item), getX() + 6, currentItemY + (itemHeight - 8) / 2, textColor);
        }

        if (hasScrollbar) {
            int scrollbarX = getX() + width - 6;
            guiGraphics.fill(scrollbarX, listY, scrollbarX + 5, listY + listHeight, 0xFF222222);

            int maxOffset   = items.size() - maxVisibleItems;
            int thumbHeight = Math.max(14, (listHeight * maxVisibleItems) / items.size());
            int thumbY      = listY + (int) (((float) scrollOffset / maxOffset) * (listHeight - thumbHeight));

            guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 5, thumbY + thumbHeight, 0xFF888888);
        }

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || !active) return false;

        if (button == 0) {
            if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= getY() && mouseY <= getY() + height) {
                open = !open;
                return true;
            }

            if (open) {
                int visibleCount = Math.min(items.size(), maxVisibleItems);
                int listHeight   = visibleCount * itemHeight;
                int listY        = getY() + height + 2;

                if (mouseX >= getX() && mouseX <= getX() + width && mouseY >= listY && mouseY <= listY + listHeight) {
                    if (items.size() > maxVisibleItems && mouseX >= getX() + width - 8) {
                        isDraggingScrollbar = true;
                        updateScrollFromMouse(mouseY, listY, listHeight);
                        return true;
                    }

                    int clickedIndex = (int) ((mouseY - listY) / itemHeight) + scrollOffset;
                    if (clickedIndex >= 0 && clickedIndex < items.size()) {
                        selectedItem = items.get(clickedIndex);
                        open = false;
                        if (onSelect != null) {
                            onSelect.accept(selectedItem);
                        }
                    }
                    return true;
                }
                open = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDraggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (open && isDraggingScrollbar && items.size() > maxVisibleItems) {
            int visibleCount = Math.min(items.size(), maxVisibleItems);
            int listHeight   = visibleCount * itemHeight;
            int listY        = getY() + height + 2;
            updateScrollFromMouse(mouseY, listY, listHeight);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void updateScrollFromMouse(double mouseY, int listY, int listHeight) {
        int   maxOffset = items.size() - maxVisibleItems;
        float progress  = Mth.clamp((float) (mouseY - listY) / listHeight, 0.0F, 1.0F);
        scrollOffset = Math.round(progress * maxOffset);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (open && items.size() > maxVisibleItems) {
            int maxOffset = items.size() - maxVisibleItems;
            scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta), 0, maxOffset);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}