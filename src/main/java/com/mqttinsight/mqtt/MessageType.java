package com.mqttinsight.mqtt;

import com.mqttinsight.util.Icons;
import lombok.Getter;

import javax.swing.*;

/**
 * @author ptma
 */
@Getter
public enum MessageType {

    RECEIVED(Icons.INCOMING),
    PUBLISHED(Icons.OUTGOING),
    RECEIVED_SCRIPT(Icons.INCOMING_SCRIPT),
    PUBLISHED_SCRIPT(Icons.OUTGOING_SCRIPT);

    private final Icon icon;

    MessageType(Icon icon) {
        this.icon = icon;
    }
}
