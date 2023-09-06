package com.mqttinsight.scripting;

import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.PublishedMqttMessage;

/**
 * @author ptma
 */
public class ScriptPubMqttMessage extends PublishedMqttMessage implements ScriptingMqttMessage {

    private final MessageType messageType = MessageType.SCRIPT_PUBLISHED;

    protected ScriptPubMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate, String format) {
        super(topic, payload, qos, retained, duplicate, format);
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    public static ScriptPubMqttMessage of(String topic, byte[] payload, int qos, boolean retained, String format) {
        return new ScriptPubMqttMessage(topic, payload, qos, retained, false, format);
    }
}
