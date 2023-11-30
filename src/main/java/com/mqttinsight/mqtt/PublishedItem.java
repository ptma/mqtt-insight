package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.ui.component.Textable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
@Setter
public class PublishedItem implements Textable {

    private String topic;

    private String payload;

    private int qos;

    private boolean retained;

    private String payloadFormat = CodecSupport.DEFAULT;

    public PublishedItem() {
    }

    public PublishedItem(final String topic, final String payload, final int qos, final boolean retained) {
        this(topic, payload, qos, retained, CodecSupport.DEFAULT);
    }

    public PublishedItem(final String topic, final String payload, final int qos, final boolean retained, final String payloadFormat) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        setPayloadFormat(payloadFormat);
    }

    public void setPayloadFormat(String payloadFormat) {
        this.payloadFormat = payloadFormat == null ? CodecSupport.DEFAULT : payloadFormat;
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
