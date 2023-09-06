package com.mqttinsight.scripting;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
public class SimpleMqttMessage {

    private String topic;
    private byte[] payload;
    private int qos;
    private boolean retained;

    public SimpleMqttMessage(String topic, byte[] payload, int qos, boolean retained) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
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
        return payload;
    }

    public String payloadAsString() {
        return new String(payload, StandardCharsets.UTF_8);
    }


}
