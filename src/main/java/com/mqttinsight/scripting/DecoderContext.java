package com.mqttinsight.scripting;

import com.mqttinsight.mqtt.Subscription;

/**
 * @author ptma
 */
public class DecoderContext {

    private final Subscription subscription;

    private final MqttMessageWrapper message;

    public DecoderContext(Subscription subscription, MqttMessageWrapper message) {
        this.subscription = subscription;
        this.message = message;
    }

    protected Subscription getSubscription() {
        return subscription;
    }

    public MqttMessageWrapper getMessage() {
        return message;
    }

}
