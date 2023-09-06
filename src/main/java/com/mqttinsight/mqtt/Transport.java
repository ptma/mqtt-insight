package com.mqttinsight.mqtt;

import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
public enum Transport implements Textable {

    MQTT("MQTT"),
    WEB_SOCKET("WebSocket");

    private final String text;

    Transport(String text) {
        this.text = text;
    }

    public static Transport of(String text) {
        for (Transport c : Transport.values()) {
            if (c.text.equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException(text);
    }

}
