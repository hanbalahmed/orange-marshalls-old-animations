package com.orangemarshall.animations.config;

import com.orangemarshall.animations.config.gui.ConfigList;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.http.ParseException;
import org.apache.logging.log4j.Level;

public abstract class ConfigurationHolder {

    private transient File configFile;
    private transient Configuration config;
    private transient String configVersion;
    private transient String configName;

    protected ConfigurationHolder(String configName, File configFile, String configVersion) {
        this.configFile = configFile;
        this.configName = configName;
        this.configVersion = configVersion;
    }

    public void openGui() {
        new DelayedGuiDisplay(1, new ConfigList(this.configName, this));
    }

    public final ArrayList getFields() {
        ArrayList list = new ArrayList();
        Field[] afield = this.getClass().getFields();
        int i = afield.length;

        for (int j = 0; j < i; ++j) {
            Field field = afield[j];

            try {
                ConfigurationHolder.ConfigOpt e = (ConfigurationHolder.ConfigOpt) field.getAnnotation(ConfigurationHolder.ConfigOpt.class);

                if (e != null && !e.ignoreInFile() && !e.ignoreInHud()) {
                    list.add(new FieldContainer(field, e, this));
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        Collections.sort(list, compare<invokedynamic>());
        return list;
    }

    public final void load() {
        try {
            this.config = new Configuration(this.configFile, this.configVersion);
            Field[] e = this.getClass().getFields();
            int i = e.length;

            for (int j = 0; j < i; ++j) {
                Field field = e[j];

                this.loadField(field, this.getFieldName(field), this.config);
            }

            this.saveConfig(this.config);
        } catch (Exception exception) {
            FMLLog.log(Level.ERROR, exception, "Error loading configuration file!", new Object[0]);
        }

    }

    public final void save() {
        try {
            Configuration e = this.config;
            Field[] afield = this.getClass().getFields();
            int i = afield.length;

            for (int j = 0; j < i; ++j) {
                Field field = afield[j];

                this.saveField(field, this.getFieldName(field), e);
            }

            this.saveConfig(e);
        } catch (Exception exception) {
            FMLLog.log(Level.ERROR, exception, "Error saving configuration file!", new Object[0]);
        }

    }

    private final String getFieldName(Field field) {
        ConfigurationHolder.ConfigOpt options = (ConfigurationHolder.ConfigOpt) field.getAnnotation(ConfigurationHolder.ConfigOpt.class);

        if (options == null) {
            return field.getName();
        } else {
            String name = options.name();

            return name.equals("") ? field.getName() : name;
        }
    }

    private final String getCategoryName(Field field) {
        ConfigurationHolder.ConfigOpt options = (ConfigurationHolder.ConfigOpt) field.getAnnotation(ConfigurationHolder.ConfigOpt.class);

        return options == null ? "client" : options.category();
    }

    private final boolean ignoreField(Field field) {
        ConfigurationHolder.ConfigOpt options = (ConfigurationHolder.ConfigOpt) field.getAnnotation(ConfigurationHolder.ConfigOpt.class);

        return options == null ? false : options.ignoreInFile();
    }

    private final String getComment(Field field) {
        ConfigurationHolder.ConfigOpt options = (ConfigurationHolder.ConfigOpt) field.getAnnotation(ConfigurationHolder.ConfigOpt.class);

        return options == null ? null : (options.comment() == "" ? null : options.comment());
    }

    private final void saveConfig(Configuration config) {
        config.save();
    }

    private final void loadField(Field field, String name, Configuration config) throws IllegalAccessException, ParseException {
        if (!Modifier.isTransient(field.getModifiers()) && !this.ignoreField(field)) {
            Object obj = null;

            if (field.getType().isAssignableFrom(Integer.TYPE)) {
                obj = Integer.valueOf(config.get(this.getCategoryName(field), name, field.getInt(this), this.getComment(field)).getInt());
            } else if (field.getType().isAssignableFrom(String.class)) {
                obj = config.get(this.getCategoryName(field), name, (String) field.get(this), this.getComment(field)).getString();
            } else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
                obj = Boolean.valueOf(config.get(this.getCategoryName(field), name, field.getBoolean(this), this.getComment(field)).getBoolean());
            } else if (field.getType().isAssignableFrom(Double.TYPE)) {
                obj = Double.valueOf(config.get(this.getCategoryName(field), name, field.getDouble(this), this.getComment(field)).getDouble());
            }

            if (obj != null) {
                field.set(this, obj);
            }

        }
    }

    private final void saveField(Field field, String name, Configuration config) throws IllegalAccessException {
        if (!Modifier.isTransient(field.getModifiers()) && !this.ignoreField(field)) {
            if (field.getType().isAssignableFrom(Integer.TYPE)) {
                config.get(this.getCategoryName(field), name, field.getInt(this), this.getComment(field)).set(field.getInt(this));
            } else if (field.getType().isAssignableFrom(String.class)) {
                config.get(this.getCategoryName(field), name, (String) field.get(this), this.getComment(field)).set((String) field.get(this));
            } else if (field.getType().isAssignableFrom(Boolean.TYPE)) {
                config.get(this.getCategoryName(field), name, field.getBoolean(this), this.getComment(field)).set(field.getBoolean(this));
            } else if (field.getType().isAssignableFrom(Double.TYPE)) {
                config.get(this.getCategoryName(field), name, field.getDouble(this), this.getComment(field)).set(field.getDouble(this));
            }

        }
    }

    public final File getConfigFile() {
        return this.configFile;
    }

    public final Configuration getConfiguration() {
        return this.config;
    }

    private static int lambda$getFields$0(FieldContainer f1, FieldContainer f2) {
        return f1.category().toLowerCase().compareTo(f2.category().toLowerCase());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD})
    protected @interface ConfigOpt {

        String name() default "";

        String category() default "client";

        String comment() default "";

        boolean ignoreInFile() default false;

        boolean ignoreInHud() default false;
    }
}
