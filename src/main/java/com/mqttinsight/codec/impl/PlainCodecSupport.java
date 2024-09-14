package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
public class PlainCodecSupport implements CodecSupport {

    @Override
    public String getName() {
        return CodecSupport.PLAIN;
    }

    @Override
    public String getSyntax() {
        return "text/plain";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        return text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
    }
}
