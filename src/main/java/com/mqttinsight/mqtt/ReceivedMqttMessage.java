package com.mqttinsight.mqtt;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.awt.*;
import java.util.Date;

/**
 * @author ptma
 */
public class ReceivedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    private final transient Subscription subscription;

    private final MessageType messageType = MessageType.RECEIVED;

    private final String topic;

    private final byte[] payload;

    private final int qos;

    private final boolean retained;

    private final boolean duplicate;

    private final Date messageTime;

    public static ReceivedMqttMessage of(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        return new ReceivedMqttMessage(subscription, topic, payload, qos, retained, duplicate);
    }

    private ReceivedMqttMessage(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        this.subscription = subscription;
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
        messageTime = new Date();
    }

    public Subscription getSubscription() {
        return subscription;
    }

    @Override
    public Color getColor() {
        return subscription.getColor();
    }

    @Override
    public String getTime() {
        return DateUtil.format(messageTime, DatePattern.NORM_DATETIME_MS_FORMAT);
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public byte[] payloadAsBytes() {
        return this.payload;
    }

    @Override
    public String getPayloadFormat() {
        return subscription.getPayloadFormat();
    }

    @Override
    public int getQos() {
        return qos;
    }

    @Override
    public boolean isRetained() {
        return retained;
    }

    @Override
    public boolean isDuplicate() {
        return duplicate;
    }

}
