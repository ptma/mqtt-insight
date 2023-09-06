package com.mqttinsight.mqtt;

import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

/**
 * @author ptma
 */

@Getter
public enum MqttQos implements Textable {

    QOS_0("0", 0),
    QOS_1("1", 1),
    QOS_2("2", 2);

    private final String text;
    private final int value;

    MqttQos(String text, int value) {
        this.text = text;
        this.value = value;
    }

    public static MqttQos of(int value) {
        for (MqttQos c : MqttQos.values()) {
            if (c.value == value) {
                return c;
            }
        }
        throw new IllegalArgumentException(String.format("Bad qos: %d", value));
    }
}
