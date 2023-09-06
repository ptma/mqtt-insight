package com.mqttinsight.codec;

import cn.hutool.core.codec.Base64;

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
    public String toString(byte[] payload) {
        return Base64.encode(payload);
    }

    @Override
    public byte[] toPayload(String text) {
        return text == null ? new byte[0] : Base64.decode(text);
    }
}
