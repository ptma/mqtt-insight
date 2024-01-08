package com.mqttinsight.codec;

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
    public String toString(byte[] payload) {
        return new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] toPayload(String text) throws CodecException {
        return text == null ? new byte[0] : text.getBytes(StandardCharsets.UTF_8);
    }
}
