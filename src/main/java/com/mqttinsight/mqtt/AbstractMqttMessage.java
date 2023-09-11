package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;

/**
 * @author ptma
 */
public abstract class AbstractMqttMessage implements MqttMessage {

    protected transient String decodeFormat;
    protected transient String decodedPayload;

    @Override
    public String decodePayload(String decodeFormat, boolean pretty) {
        CodecSupport codec = CodecSupports.instance().getByName(decodeFormat);
        return decodePayload(codec, pretty);
    }
    
    @Override
    public String decodePayload(CodecSupport codec, boolean pretty) {
        if (this.decodeFormat == null || !this.decodeFormat.equals(decodeFormat) || decodedPayload == null) {
            this.decodeFormat = decodeFormat;
            decodedPayload = codec.toString(payloadAsBytes());
        }
        if (pretty) {
            return codec.toPrettyString(decodedPayload);
        } else {
            return decodedPayload;
        }
    }
}
