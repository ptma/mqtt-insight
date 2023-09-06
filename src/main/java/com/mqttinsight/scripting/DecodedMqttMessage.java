package com.mqttinsight.scripting;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.AbstractMqttMessage;
import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.Subscription;

import java.awt.*;
import java.util.Date;

/**
 * @author ptma
 */
public class DecodedMqttMessage extends AbstractMqttMessage implements MqttMessage, ScriptingMqttMessage {

    private transient Subscription subscription;

    private final MessageType messageType = MessageType.SCRIPT_DECODED;

    private String topic;
    private int qos;
    private boolean retained;
    private byte[] payload;
    private String format;

    private final Date messageTime;

    public DecodedMqttMessage() {
        messageTime = new Date();
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public byte[] payloadAsBytes() {
        return payload;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    @Override
    public boolean isRetained() {
        return retained;
    }

    @Override
    public boolean isDuplicate() {
        return false;
    }

    @Override
    public String getTime() {
        return DateUtil.format(messageTime, DatePattern.NORM_DATETIME_MS_FORMAT);
    }

    @Override
    public Color getColor() {
        return subscription.getColor();
    }

    public void setRetained(boolean retained) {
        this.retained = retained;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public String getPayloadFormat() {
        return format != null ? format :
            (
                subscription != null ? subscription.getPayloadFormat() : CodecSupport.PLAIN
            );
    }

    public static DecodedMqttMessage copyFrom(SimpleMqttMessage message) {
        DecodedMqttMessage m = new DecodedMqttMessage();
        m.setTopic(message.getTopic());
        m.setPayload(message.getPayload());
        m.setQos(message.getQos());
        m.setRetained(message.isRetained());
        return m;
    }

}
