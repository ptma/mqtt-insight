package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;
import org.apache.commons.codec.binary.Base64;

/**
 * @author ptma
 */
public class Base64EncoderCodecSupport implements CodecSupport {


    @Override
    public String getName() {
        return "Base64 Encoder";
    }

    @Override
    public String getSyntax() {
        return "text/plain";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        return Base64.encodeBase64String(payload);
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        return text == null ? new byte[0] : Base64.encodeBase64(text.getBytes());
    }
}
