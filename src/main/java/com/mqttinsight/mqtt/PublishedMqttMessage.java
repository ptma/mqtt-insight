package com.mqttinsight.mqtt;

/**
 * @author ptma
 */
public class PublishedMqttMessage extends AbstractMqttMessage implements MqttMessage {

    private final MessageType messageType = MessageType.PUBLISHED;

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

}
