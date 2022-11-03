package com.orangemarshall.animations.config.gui;

import com.orangemarshall.animations.config.FieldContainer;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

public class SlotConfigList extends ScrollingList {

    private ConfigList parent;
    private ArrayList entries;
    private static final int entryHeight = 15;

    public SlotConfigList(ConfigList parent, ArrayList entries, int listWidth) {
        super(Minecraft.getMinecraft(), listWidth, parent.height, 24, parent.height - 35, 10, 15, parent.width, parent.height);
        this.parent = parent;
        this.entries = entries;
    }

    public int listWidth() {
        return this.listWidth;
    }

    public int right() {
        return this.right;
    }

    public int bottom() {
        return this.bottom;
    }

    protected int getSize() {
        return this.entries.size();
    }

    protected void elementClicked(int index, boolean doubleClick) {
        this.parent.selectModIndex(index);
    }

    protected boolean isSelected(int index) {
        return this.parent.fieldIndexSelected(index);
    }

    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    protected int getContentHeight() {
        return this.getSize() * 15 + 1;
    }

    protected void drawSlot(int idx, int right, int top, int height, Tessellator tess) {
        FieldContainer mc = (FieldContainer) this.entries.get(idx);
        String name = mc.name();
        FontRenderer font = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name, this.listWidth - 10), this.left + 3, top + 2, 16777215);
    }
}
