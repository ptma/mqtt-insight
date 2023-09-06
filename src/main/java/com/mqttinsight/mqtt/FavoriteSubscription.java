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

    private String payloadFormat = CodecSupport.AUTO;

    public FavoriteSubscription() {
    }

    public FavoriteSubscription(final String topic, final int qos) {
        this(topic, qos, CodecSupport.AUTO);
    }

    public FavoriteSubscription(final String topic, final int qos, final String payloadFormat) {
        this.topic = topic;
        this.qos = qos;
        this.payloadFormat = payloadFormat;
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
