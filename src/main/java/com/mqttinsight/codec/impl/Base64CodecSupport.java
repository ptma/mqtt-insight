package com.mqttinsight.codec.impl;

import cn.hutool.core.codec.Base64;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;

/**
 * @author ptma
 */
public class Base64CodecSupport implements CodecSupport {


    @Override
    public String getName() {
        return "Base64";
    }

    @Override
    public String getSyntax() {
        return "text/plain";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        return Base64.encode(payload);
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        return text == null ? new byte[0] : Base64.decode(text);
    }
}
