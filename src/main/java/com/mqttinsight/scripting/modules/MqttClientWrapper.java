package com.mqttinsight.scripting.modules;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.scripting.ScriptPubMqttMessage;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Utils;

/**
 * @author ptma
 */
public class MqttClientWrapper {

    private final MqttInstance mqttInstance;

    public MqttClientWrapper(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
    }

    public void subscribe(String topic) {
        subscribe(topic, 0);
    }

    public void subscribe(String topic, int qos) {
        Subscription subscription = new Subscription(mqttInstance, topic, qos, CodecSupport.DEFAULT, Utils.generateRandomColor());
        mqttInstance.subscribe(subscription);
    }

    public void publish(String topic, String payload) {
        publish(topic, payload.getBytes(), 0, false);
    }

    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, String payload, int qos) {
        publish(topic, payload.getBytes(), qos, false);
    }

    public void publish(String topic, byte[] payload, int qos) {
        publish(topic, payload, qos, false);
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        publish(topic, payload.getBytes(), qos, retained);
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        mqttInstance.publishMessage(ScriptPubMqttMessage.of(
            topic,
            payload,
            qos,
            retained,
            CodecSupport.PLAIN
        ));
    }

}
