package com.mqttinsight.mqtt;

import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
public enum Version implements Textable {

    MQTT_3_1("MQTT 3.1"),
    MQTT_3_1_1("MQTT 3.1.1"),
    MQTT_5("MQTT 5");

    private final String text;

    Version(String text) {
        this.text = text;
    }

    public static Version of(String text) {
        for (Version c : Version.values()) {
            if (c.text.equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException(text);
    }

}
