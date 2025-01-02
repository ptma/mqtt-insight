package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;
import org.bouncycastle.util.encoders.Hex;

/**
 * @author ptma
 */
public class HexCodecSupport implements CodecSupport {

    @Override
    public String getName() {
        return "HEX";
    }

    @Override
    public String getSyntax() {
        return "text/plain";
    }

    @Override
    public String toString(String topic, byte[] payload) {
        return toHexString(payload);
    }

    @Override
    public byte[] toPayload(String topic, String text) throws CodecException {
        return text == null ? new byte[0] : Hex.decode(text.replaceAll("\\s", ""));
    }

    private String toHexString(byte[] payload) {
        StringBuilder builder = new StringBuilder(payload.length * 3);
        for (int i = 0; i < payload.length; i++) {
            builder.append(String.format("%02X ", payload[i]));
            if ((i + 1) % 16 == 0) {
                builder.append("\n");
            } else if ((i + 1) % 8 == 0) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}
