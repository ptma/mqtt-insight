package com.mqttinsight.config;

import com.formdev.flatlaf.FlatLightLaf;
import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
public enum Themes implements Textable {

    LIGHT(FlatLightLaf.NAME, FlatLightLaf.class.getName(), null, false),
    DARK("One Dark", null, "one_dark.theme.json", true);

    private final String text;
    private final String lafClassName;
    private final String resourceName;
    private final boolean dark;

    Themes(String text, String lafClassName, String resourceName, boolean dark) {
        this.text = text;
        this.lafClassName = lafClassName;
        this.resourceName = resourceName;
        this.dark = dark;
    }

    @Override
    public String toString() {
        return name();
    }

    public static Themes of(String name) {
        for (Themes c : Themes.values()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return Themes.LIGHT;
    }

}
