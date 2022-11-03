package com.orangemarshall.animations.util;

import java.lang.reflect.Field;

public class FieldWrapper {

    private static Field modifiersField;
    private Field field;

    public FieldWrapper(String fieldName, Class clazz) {
        try {
            this.field = clazz.getDeclaredField(fieldName);
            this.field.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException nosuchfieldexception) {
            nosuchfieldexception.printStackTrace();
        }

    }

    public Object get(Object obj) {
        try {
            return this.field.get(obj);
        } catch (IllegalAccessException | IllegalArgumentException illegalargumentexception) {
            illegalargumentexception.printStackTrace();
            return null;
        }
    }

    public void set(Object obj, Object value) {
        try {
            this.field.set(obj, value);
        } catch (IllegalAccessException | IllegalArgumentException illegalargumentexception) {
            illegalargumentexception.printStackTrace();
        }

    }

    public void setFinal(Object obj, Object value) {
        try {
            FieldWrapper.modifiersField.setInt(this.field, this.field.getModifiers() & -17);
            this.field.set(obj, value);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    static {
        try {
            FieldWrapper.modifiersField = Field.class.getDeclaredField("modifiers");
            FieldWrapper.modifiersField.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException nosuchfieldexception) {
            nosuchfieldexception.printStackTrace();
        }

    }
}
