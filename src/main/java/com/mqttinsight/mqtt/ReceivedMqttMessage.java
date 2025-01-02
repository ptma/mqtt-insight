package com.mqttinsight.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mqttinsight.codec.CodecSupport;

import java.awt.*;

/**
 * @author ptma
 */
public class ReceivedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    @JsonIgnore
    protected transient Subscription subscription;

    private MessageType messageType = MessageType.RECEIVED;

    protected String format;

    public static ReceivedMqttMessage of(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        return new ReceivedMqttMessage(subscription, topic, payload, qos, retained, duplicate);
    }

    protected ReceivedMqttMessage(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        super(topic, payload, qos, retained, duplicate);
        this.subscription = subscription;
    }

    @JsonIgnore
    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    @Override
    @JsonIgnore
    public Color getColor() {
        return super.getColor() != null ? super.getColor() : subscription == null ? null : subscription.getColor();
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getPayloadFormat() {
        if (format == null || format.equals(CodecSupport.DEFAULT)) {
            return subscription == null ? CodecSupport.DEFAULT : subscription.getPayloadFormat();
        } else {
            return format;
        }
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
