package com.mqttinsight.scripting;

import cn.hutool.core.img.ColorUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.mqtt.Subscription;

import java.awt.*;

/**
 * @author ptma
 */
public class DecodedMqttMessage extends ReceivedMqttMessage {

    private final MessageType messageType = MessageType.RECEIVED_SCRIPT;

    private String color;

    public static DecodedMqttMessage of(Subscription subscription, MqttMessageWrapper message) {
        return new DecodedMqttMessage(
            subscription,
            message.getTopic(),
            message.getPayload(),
            message.getQos(),
            message.isRetained(),
            message.isDuplicate()
        );
    }

    public DecodedMqttMessage(Subscription subscription, String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        super(subscription, topic, payload, qos, retained, duplicate);
    }

    @Override
    public Color getColor() {
        return color != null ? ColorUtil.hexToColor(color) : (
            subscription == null ? null : subscription.getColor()
        );
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String getPayloadFormat() {
        return format != null ? format : (
            subscription != null ? subscription.getPayloadFormat() : CodecSupport.PLAIN
        );
    }

    public void setColor(String color) {
        this.color = color;
    }

}
