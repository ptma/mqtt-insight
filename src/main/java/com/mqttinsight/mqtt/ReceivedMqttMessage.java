package com.mqttinsight.mqtt;

import java.awt.*;

/**
 * @author ptma
 */
public class ReceivedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    private final transient Subscription subscription;

    private final MessageType messageType = MessageType.RECEIVED;

    public static ReceivedMqttMessage of(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        return new ReceivedMqttMessage(subscription, topic, payload, qos, retained, duplicate);
    }

    private ReceivedMqttMessage(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        super(topic, payload, qos, retained, duplicate);
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public Color getColor() {
        return subscription.getColor();
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getPayloadFormat() {
        return subscription.getPayloadFormat();
    }

}
