package com.mqttinsight.config;

import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

import java.util.Locale;

/**
 * @author ptma
 */
@Getter
public enum Languages implements Textable {

    ENGLISH("English", Locale.ENGLISH.toLanguageTag()),
    SIMPLIFIED_CHINESE("简体中文", Locale.SIMPLIFIED_CHINESE.toLanguageTag());

    private final String text;

    private final String languageTag;

    Languages(String text, String languageTag) {
        this.text = text;
        this.languageTag = languageTag;
    }

    @Override
    public String toString() {
        return languageTag;
    }

    public static Languages of(String languageTag) {
        for (Languages c : Languages.values()) {
            if (c.languageTag.equalsIgnoreCase(languageTag)) {
                return c;
            }
        }
        return Languages.ENGLISH;
    }

}
