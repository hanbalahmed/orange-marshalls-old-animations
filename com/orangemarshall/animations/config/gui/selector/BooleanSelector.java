package com.orangemarshall.animations.config.gui.selector;

import net.minecraft.client.gui.Gui;

public class BooleanSelector extends Selector {

    private final int id;
    public int xPosition;
    public int yPosition;
    public int width;
    public int height;
    private boolean value;

    public BooleanSelector(int componentId, int x, int y) {
        this.id = componentId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = 20;
        this.height = 20;
    }

    public String getValue() {
        return "" + this.value;
    }

    public int getId() {
        return this.id;
    }

    public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
        boolean flag = p_146192_1_ >= this.xPosition && p_146192_1_ < this.xPosition + this.width && p_146192_2_ >= this.yPosition && p_146192_2_ < this.yPosition + this.height;

        if (flag) {
            this.value = !this.value;
        }

    }

    public void drawCheckbox() {
        drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, -6250336);
        drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
        byte padding = 1;

        if (this.value) {
            Gui.drawRect(this.xPosition + padding, this.yPosition + padding, this.xPosition + this.width - padding, this.yPosition + this.height - padding, -3092272);
        }

    }

    public int getWidth() {
        return this.width;
    }

    public void draw() {
        this.drawCheckbox();
    }

    public void setValue(Object value) {
        this.value = Boolean.valueOf(value.toString()).booleanValue();
    }

    public void keyTyped(char c, int keyCode) {}
}
