package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;
import org.bouncycastle.util.encoders.Hex;

import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ptma
 */
public class HexCodecSupport implements CodecSupport {

    static Pattern SEGMENT_PATTERN = Pattern.compile("[0-9a-fA-F]{2}");

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

    @Override
    public String toPrettyString(String payload) {
        Matcher matcher = SEGMENT_PATTERN.matcher(payload);
        int count = 0;

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group()).append(" ");
            count++;
            if (count % 16 == 0) {
                sb.append("\n");
            } else if (count % 8 == 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toHexString(byte[] payload) {
        StringJoiner joiner = new StringJoiner(" ");
        for (byte b : payload) {
            joiner.add(String.format("%02X", b));
        }
        return joiner.toString();
    }
}
