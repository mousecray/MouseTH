package ru.mousecray.mouseth.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import ru.mousecray.mouseth.gui.widget.HelpWidget;

import java.util.ArrayList;
import java.util.List;

public class OptionsScrollList extends ContainerObjectSelectionList<OptionsScrollList.Entry> {
    public OptionsScrollList(Minecraft minecraft, int width, int height, int y0, int itemHeight) {
        super(minecraft, width, height, y0, y0 + height, itemHeight);
    }

    @Override
    public int getRowWidth() {
        return 340;
    }

    @Override
    protected int getScrollbarPosition() {
        return (width + getRowWidth()) / 2 + 6;
    }

    public void addHeader(Component title, int color) {
        addEntry(new Entry(title, color));
    }

    public void addOption(AbstractWidget widget) {
        addEntry(new Entry(null, widget, null, null));
    }

    public void addOption(AbstractWidget widget, HelpWidget help) {
        addEntry(new Entry(null, widget, help, null));
    }

    public void addOptionWithSubNote(AbstractWidget widget, Component subNote, HelpWidget help) {
        addEntry(new Entry(null, widget, help, subNote));
    }

    public void addOptionWithLabel(Component label, AbstractWidget widget, HelpWidget help) {
        addEntry(new Entry(label, widget, help, null));
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private final Component      label;
        private final AbstractWidget widget;
        private final HelpWidget     help;
        private final Component      subNote;
        private final boolean        isHeader;
        private final int            headerColor;

        public Entry(Component header, int headerColor) {
            label = header;
            widget = null;
            help = null;
            subNote = null;
            isHeader = true;
            this.headerColor = headerColor;
        }

        public Entry(Component label, AbstractWidget widget, HelpWidget help, Component subNote) {
            this.label = label;
            this.widget = widget;
            this.help = help;
            this.subNote = subNote;
            isHeader = false;
            headerColor = 0xFFFFFF;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            if (isHeader) {
                guiGraphics.drawCenteredString(Minecraft.getInstance().font, label, left + width / 2, top + (height - 8) / 2, headerColor);
                return;
            }

            int rightEdge = left + width;

            if (help != null) {
                int helpY = (subNote != null) ? (top + (20 - 14) / 2) : (top + (height - 14) / 2);
                help.setX(rightEdge - 14);
                help.setY(helpY);
                help.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            int availableRight = (help != null) ? (rightEdge - 20) : rightEdge;

            if (label != null) {
                int textColor = (widget != null && !widget.active) ? 0xFF777777 : 0xFFFFFF;
                guiGraphics.drawString(Minecraft.getInstance().font, label, left, top + (height - 8) / 2, textColor);
                if (widget != null) {
                    widget.setX(availableRight - widget.getWidth());
                    widget.setY(top + (height - widget.getHeight()) / 2);
                    widget.render(guiGraphics, mouseX, mouseY, partialTick);
                }
            } else if (widget != null) {
                int widgetY = (subNote != null) ? top : (top + (height - widget.getHeight()) / 2);
                widget.setX(left);
                widget.setWidth(availableRight - left);
                widget.setY(widgetY);
                widget.render(guiGraphics, mouseX, mouseY, partialTick);

                if (subNote != null) {
                    guiGraphics.pose().pushPose();
                    float scale = 0.75F;
                    int   noteX = left + 20;
                    int   noteY = top + 21;
                    guiGraphics.pose().translate(noteX, noteY, 0);
                    guiGraphics.pose().scale(scale, scale, 1.0F);
                    guiGraphics.drawString(Minecraft.getInstance().font, subNote, 0, 0, 0xFF5555, false);
                    guiGraphics.pose().popPose();
                }
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            List<GuiEventListener> list = new ArrayList<>();
            if (widget != null) list.add(widget);
            if (help != null) list.add(help);
            return list;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            List<NarratableEntry> list = new ArrayList<>();
            if (widget != null) list.add(widget);
            if (help != null) list.add(help);
            return list;
        }
    }
}