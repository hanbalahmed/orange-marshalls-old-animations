package com.orangemarshall.animations.config.gui;

import com.orangemarshall.animations.config.ConfigurationHolder;
import com.orangemarshall.animations.config.FieldContainer;
import java.lang.reflect.Field;

public class SpacerContainer extends FieldContainer {

    private String name;

    public SpacerContainer(String name, ConfigurationHolder config) {
        super((Field) null, (ConfigurationHolder.ConfigOpt) null, config);
        this.name = "               " + name;
    }

    public String name() {
        return this.name;
    }

    public String category() {
        return "null";
    }

    public Field getField() {
        return this.field;
    }

    public void setValue(String value) {}

    public String getValue() {
        return "";
    }
}
