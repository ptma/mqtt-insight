package com.mqttinsight.scripting;

import com.mqttinsight.mqtt.MqttMessage;

/**
 * @author ptma
 */
public interface DecoderCallback {

    void onDecoderResult(MqttMessage decodedMessage);
}
