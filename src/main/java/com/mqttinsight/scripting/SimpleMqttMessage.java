package com.mqttinsight.scripting;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
public class SimpleMqttMessage {

    private final String topic;
    private final byte[] payload;
    private final int qos;
    private final boolean retained;
    private final boolean duplicate;

    public SimpleMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
    }

    public String getTopic() {
        return topic;
    }

    public int getQos() {
        return qos;
    }

    public boolean isRetained() {
        return retained;
    }

    public byte[] getPayload() {
        return this.payload == null ? new byte[0] : payload;
    }

    public String payloadAsString() {
        return new String(getPayload(), StandardCharsets.UTF_8);
    }

    public boolean isDuplicate() {
        return duplicate;
    }
}
