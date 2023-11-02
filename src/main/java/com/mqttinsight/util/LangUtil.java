package com.mqttinsight.util;

import javax.swing.*;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author ptma
 */
public class LangUtil {

    private static ResourceBundle bundle;

    public static ResourceBundle getBundle() {
        if (bundle != null) {
            return bundle;
        }
        bundle = ResourceBundle.getBundle("com/mqttinsight/Lang");
        return bundle;
    }

    public static void setLocale(Locale locale) {
        Locale.setDefault(locale);
        bundle = ResourceBundle.getBundle("com/mqttinsight/Lang", locale);
    }

    public static String getString(String key) {
        ResourceBundle bundle = getBundle();
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return key;
        }
    }

    public static String getString(String key, String defaultValue) {
        ResourceBundle bundle = getBundle();
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return defaultValue;
        }
    }

    public static String format(String key, Object... args) {
        ResourceBundle bundle = getBundle();
        if (bundle.containsKey(key)) {
            return String.format(bundle.getString(key), args);
        } else {
            return key;
        }
    }

    public static boolean contains(String key) {
        return getBundle().containsKey(key);
    }

    public static void buttonText(AbstractButton component, String key) {
        Utils.UI.buttonText(component, getBundle().getString(key));
    }
}
