package com.orangemarshall.animations.config.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class Buttons {

    public static class GuiButtonCancel extends GuiButton {

        public GuiButtonCancel(int buttonId, int x, int y, int buttonWidth) {
            super(buttonId, x, y, buttonWidth, 20, I18n.format("Cancel", new Object[0]));
        }
    }

    public static class GuiButtonReset extends GuiButton {

        public GuiButtonReset(int buttonId, int x, int y, int buttonWidth) {
            super(buttonId, x, y, buttonWidth, 20, I18n.format("Reset", new Object[0]));
        }
    }

    public static class GuiButtonDone extends GuiButton {

        public GuiButtonDone(int buttonId, int x, int y, int buttonWidth) {
            super(buttonId, x, y, buttonWidth, 20, I18n.format("gui.done", new Object[0]));
        }
    }
}
