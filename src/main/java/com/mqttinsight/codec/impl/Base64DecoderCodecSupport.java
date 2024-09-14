package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;
import org.apache.commons.codec.binary.Base64;

/**
 * @author ptma
 */
public class Base64DecoderCodecSupport implements CodecSupport {


    @Override
    public String getName() {
        return "Base64 Decoder";
    }

    @Override
    public String getSyntax() {
        return "text/plain";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        return new String(Base64.decodeBase64(payload));
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        return text == null ? new byte[0] : Base64.decodeBase64(text);
    }
}
