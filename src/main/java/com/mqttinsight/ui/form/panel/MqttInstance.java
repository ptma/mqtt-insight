package com.mqttinsight.ui.form.panel;

import com.mqttinsight.mqtt.*;
import com.mqttinsight.ui.component.MessageTable;
import com.mqttinsight.ui.event.InstanceEventListener;

import java.util.List;

/**
 * @author ptma
 */
public interface MqttInstance {

    MqttProperties getProperties();

    void initMqttClient();

    void connect();

    void disconnect();

    boolean isConnected();

    ConnectionStatus getConnectionStatus();

    boolean subscribe(final Subscription subscription);

    boolean unsubscribe(final Subscription subscription);

    void setPayloadFormat(String format);

    String getPayloadFormat();

    MessageTable getMessageTable();

    void addEventListeners(InstanceEventListener eventListener);

    List<InstanceEventListener> getEventListeners();

    void messageReceived(MqttMessage message);

    void previewMessage(MqttMessage message);

    void publishMessage(PublishedMqttMessage message);

    void close();
}
