package com.mqttinsight.mqtt;

import com.mqttinsight.util.Icons;
import lombok.Getter;

import javax.swing.*;

/**
 * @author ptma
 */
@Getter
public enum MessageType {

    RECEIVED(Icons.ARROW_INCOMING),
    PUBLISHED(Icons.ARROW_OUTGOING),
    SCRIPT_DECODED(Icons.JAVASCRIPT_GREEN),
    SCRIPT_PUBLISHED(Icons.JAVASCRIPT_ORANGE);

    private final Icon icon;

    MessageType(Icon icon) {
        this.icon = icon;
    }
}
