package com.mqttinsight.scripting;

import com.mqttinsight.mqtt.Subscription;

/**
 * @author ptma
 */
public class DecoderContext {

    private final Subscription subscription;

    private final SimpleMqttMessage message;

    public DecoderContext(Subscription subscription, SimpleMqttMessage message) {
        this.subscription = subscription;
        this.message = message;
    }

    protected Subscription getSubscription() {
        return subscription;
    }

    public SimpleMqttMessage getMessage() {
        return message;
    }

}
