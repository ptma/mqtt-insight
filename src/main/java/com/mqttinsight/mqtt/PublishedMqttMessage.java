package com.mqttinsight.mqtt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.swing.*;
import java.awt.*;

/**
 * @author ptma
 */
public class PublishedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    private static final Color PUBLISH_BG = UIManager.getBoolean("laf.dark") ? Color.decode("#133918") : Color.decode("#C5EBCA");

    private final MessageType messageType = MessageType.PUBLISHED;

    @JsonIgnore
    protected final transient String format;

    public static PublishedMqttMessage of(String topic, byte[] payload, int qos, boolean retained, String format) {
        return new PublishedMqttMessage(topic, payload, qos, retained, false, format);
    }

    protected PublishedMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate, String format) {
        super(topic, payload, qos, retained, duplicate);
        this.format = format;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getPayloadFormat() {
        return format;
    }

    @Override
    public Color getColor() {
        Color color = super.getColor();
        return color == null ? PUBLISH_BG : color;
    }
}
