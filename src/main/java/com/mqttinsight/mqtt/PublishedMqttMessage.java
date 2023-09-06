package com.mqttinsight.mqtt;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.util.Date;

/**
 * @author ptma
 */
public class PublishedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    private final MessageType messageType = MessageType.PUBLISHED;

    protected final String topic;

    protected final byte[] payload;

    protected final int qos;

    protected final boolean retained;

    protected final boolean duplicate;

    protected final Date messageTime;

    protected final transient String format;

    public static PublishedMqttMessage of(String topic, byte[] payload, int qos, boolean retained, String format) {
        return new PublishedMqttMessage(topic, payload, qos, retained, false, format);
    }

    protected PublishedMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate, String format) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
        this.format = format;
        messageTime = new Date();
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
        return format;
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
