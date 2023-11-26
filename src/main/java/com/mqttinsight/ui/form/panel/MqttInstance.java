package com.mqttinsight.ui.form.panel;

import com.mqttinsight.mqtt.*;
import com.mqttinsight.ui.component.MessageTable;
import com.mqttinsight.ui.event.InstanceEventListener;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author ptma
 */
public interface MqttInstance {

    MqttProperties getProperties();

    void initMqttClient();

    void connect();

    void disconnect(boolean withFail);

    boolean isConnected();

    ConnectionStatus getConnectionStatus();

    boolean subscribe(final Subscription subscription);

    void unsubscribe(final Subscription subscription, Consumer<Boolean> unsubscribed);

    void setPayloadFormat(String format);

    String getPayloadFormat();

    MessageTable getMessageTable();

    default List<MqttMessage> getMessage() {
        return getMessageTable().getMessage();
    }

    void addEventListener(InstanceEventListener eventListener);

    void removeEventListener(InstanceEventListener eventListener);

    void applyEvent(Consumer<InstanceEventListener> action);

    void messageReceived(MqttMessage message);

    void publishMessage(PublishedMqttMessage message);

    void close();
}
