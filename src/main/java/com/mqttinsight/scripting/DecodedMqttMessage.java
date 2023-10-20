package com.mqttinsight.scripting;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.AbstractMqttMessage;
import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.Subscription;

import java.awt.*;

/**
 * @author ptma
 */
public class DecodedMqttMessage extends AbstractMqttMessage implements MqttMessage, ScriptingMqttMessage {

    private transient Subscription subscription;

    private final MessageType messageType = MessageType.SCRIPT_DECODED;

    private byte[] payload;

    private String format;

    public DecodedMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        super(topic, payload, qos, retained, duplicate);
        this.payload = payload;
    }

    @Override
    public Color getColor() {
        return subscription.getColor();
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

    @Override
    public byte[] payloadAsBytes() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public static DecodedMqttMessage copyFrom(SimpleMqttMessage message) {
        return new DecodedMqttMessage(
            message.getTopic(),
            message.getPayload(),
            message.getQos(),
            message.isRetained(),
            message.isDuplicate()
        );
    }

}
