package com.mqttinsight.mqtt;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;

import java.util.Date;

/**
 * @author ptma
 */
public abstract class AbstractMqttMessage implements MqttMessage {

    private final String topic;

    private final byte[] payload;

    private final int qos;

    private final boolean retained;

    private final boolean duplicate;

    private final Date time;

    protected transient String decodeFormat;
    protected transient String decodedPayload;

    public AbstractMqttMessage(String topic, byte[] payload, int qos, boolean retained, boolean duplicate) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.retained = retained;
        this.duplicate = duplicate;
        time = new Date();
    }

    @Override
    public String decodePayload(String decodeFormat, boolean pretty) {
        CodecSupport codec = CodecSupports.instance().getByName(decodeFormat);
        return decodePayload(codec, pretty);
    }

    @Override
    public String decodePayload(CodecSupport codec, boolean pretty) {
        if (this.decodeFormat == null || !this.decodeFormat.equals(codec.getName()) || decodedPayload == null) {
            this.decodeFormat = codec.getName();
            decodedPayload = codec.toString(payloadAsBytes());
        }
        if (pretty) {
            return codec.toPrettyString(decodedPayload);
        } else {
            return decodedPayload;
        }
    }

    @Override
    public String getTime() {
        return DateUtil.format(time, DatePattern.NORM_DATETIME_MS_FORMAT);
    }

    @Override
    public long getTimestamp() {
        return time.getTime();
    }

    @Override
    public String timeWithFormat(String dateTimeFormat) {
        return DateUtil.format(time, dateTimeFormat);
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public byte[] payloadAsBytes() {
        return this.payload;
    }

    @Override
    public int getQos() {
        return qos;
    }

    @Override
    public boolean isRetained() {
        return retained;
    }

    @Override
    public boolean isDuplicate() {
        return duplicate;
    }
}
