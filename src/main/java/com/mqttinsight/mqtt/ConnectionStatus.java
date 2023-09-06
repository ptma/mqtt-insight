package com.mqttinsight.mqtt;

import com.mqttinsight.util.Icons;
import lombok.Getter;

import javax.swing.*;

/**
 * @author ptma
 */
@Getter
public enum ConnectionStatus {
    CONNECTING("Connecting", Icons.STATUS_CONNECTING, Icons.STATUS_CONNECTING_SMALL),
    CONNECTED("Connected", Icons.STATUS_CONNECTED, Icons.STATUS_CONNECTED_SMALL),
    DISCONNECTING("Disconnecting", Icons.STATUS_DISCONNECTING, Icons.STATUS_DISCONNECTING_SMALL),
    DISCONNECTED("Disconnected", Icons.STATUS_DISCONNECTED, Icons.STATUS_DISCONNECTED_SMALL),
    FAILED("Failed", Icons.STATUS_FAILED, Icons.STATUS_FAILED_SMALL);

    private final String text;
    private final Icon icon;
    private final Icon smallIcon;

    ConnectionStatus(String text, Icon icon, Icon smallIcon) {
        this.text = text;
        this.icon = icon;
        this.smallIcon = smallIcon;
    }

}
