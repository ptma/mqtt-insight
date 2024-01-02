package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.ui.component.Textable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
@EqualsAndHashCode
public class FavoriteSubscription implements Textable {

    private String topic;

    private int qos;

    private String payloadFormat;

    public FavoriteSubscription(final String topic, final int qos) {
        this(topic, qos, CodecSupport.DEFAULT);
    }

    public FavoriteSubscription(final String topic, final int qos, final String payloadFormat) {
        this.topic = topic;
        this.qos = qos;
        this.payloadFormat = payloadFormat;
    }

    public void setPayloadFormat(String payloadFormat) {
        this.payloadFormat = payloadFormat == null || "Auto".equals(payloadFormat) ? CodecSupport.DEFAULT : payloadFormat;
    }

    @Override
    public String getText() {
        return topic;
    }

    @Override
    public String toString() {
        return topic;
    }
}
