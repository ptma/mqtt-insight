package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import java.awt.*;

/**
 * @author ptma
 */
public interface MqttMessage {

    MessageType getMessageType();

    String getTopic();

    byte[] payloadAsBytes();

    String getPayloadFormat();

    String decodePayload(String format, boolean pretty);
    
    String decodePayload(CodecSupport codec, boolean pretty);

    default String payloadAsString(boolean pretty) {
        return decodePayload(getPayloadFormat(), pretty);
    }

    default String getPayload() {
        return payloadAsString(false);
    }

    int getQos();

    boolean isRetained();

    boolean isDuplicate();

    String getTime();

    default Color getColor() {
        return null;
    }

}
