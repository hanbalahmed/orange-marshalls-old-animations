package com.orangemarshall.animations.config.gui.selector;

import net.minecraft.client.gui.FontRenderer;

public class NumberSelector extends TextSelector {

    private boolean allowDecimals;

    public NumberSelector(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height, boolean allowDecimals) {
        super(componentId, fontrendererObj, x, y, width, height);
        this.allowDecimals = allowDecimals;
    }

    public void keyTyped(char c, int keyCode) {
        boolean flag = Character.isDigit(c);

        flag |= this.allowDecimals && c == 46;
        flag |= c == 8 || c == 127;
        if (flag) {
            this.textboxKeyTyped(c, keyCode);
        }

    }
}
