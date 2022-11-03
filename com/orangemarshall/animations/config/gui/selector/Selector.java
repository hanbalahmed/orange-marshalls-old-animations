package com.orangemarshall.animations.config.gui.selector;

import com.orangemarshall.animations.config.FieldContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

public abstract class Selector extends Gui {

    protected static final int COMPONENT_ID = 1337;
    protected static final FontRenderer RENDERER = Minecraft.getMinecraft().fontRendererObj;

    public abstract String getValue();

    public static Selector getFromField(FieldContainer field, int left, int top, int maxWidth) {
        if (field == null) {
            return null;
        } else {
            Class value = field.getField().getType();
            Object selector = null;

            if (value.isAssignableFrom(Integer.TYPE)) {
                selector = new NumberSelector(1337, Selector.RENDERER, left, top, maxWidth, 20, false);
            } else if (value.isAssignableFrom(Double.TYPE)) {
                selector = new NumberSelector(1337, Selector.RENDERER, left, top, maxWidth, 20, true);
            } else if (value.isAssignableFrom(Boolean.TYPE)) {
                int x = left + (maxWidth - 20) / 2;

                selector = new BooleanSelector(1337, x, top);
            } else if (value.isAssignableFrom(String.class)) {
                selector = new TextSelector(1337, Selector.RENDERER, left, top, maxWidth, 20);
            }

            if (selector == null) {
                throw new IllegalArgumentException("Could not find Selector for " + field.getClass());
            } else {
                ((Selector) selector).setValue(field.getValue());
                return (Selector) selector;
            }
        }
    }

    public abstract void draw();

    public abstract void setValue(Object object);

    public abstract void keyTyped(char c0, int i);

    public abstract void mouseClicked(int i, int j, int k);
}
