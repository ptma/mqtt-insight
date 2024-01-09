package com.mqttinsight.codec.impl;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.exception.CodecException;

/**
 * @author ptma
 */
public class JsonCodecSupport extends PlainCodecSupport implements CodecSupport {

    @Override
    public String getName() {
        return CodecSupport.JSON;
    }

    @Override
    public String getSyntax() {
        return "text/json";
    }

    @Override
    public String toPrettyString(String payload) {
        return prettyPrint(payload);
    }

    @Override
    public byte[] toPayload(String json) throws CodecException {
        return super.toPayload(json);
    }

    protected String prettyPrint(String json) {
        int indent = 0;
        boolean inString = false;
        boolean inEscape = false;
        StringBuilder result = new StringBuilder();
        final char[] chars = json.toCharArray();
        for (char c : chars) {
            if (c == '\n' || c == '\r' || (!inString && (c == ' ' || c == '\t'))) {
                continue;
            }
            switch (c) {
                case '}':
                case ']':
                    if (!inString) {
                        result.append("\n").append(indentString(--indent));
                    }
                    break;
            }
            result.append(c);
            switch (c) {
                case '{':
                case '[':
                    if (!inString) {
                        result.append("\n").append(indentString(++indent));
                    }
                    break;
                case ':':
                    if (!inString) {
                        result.append(" ");
                    }
                    break;
                case ',':
                    if (!inString) {
                        result.append("\n").append(indentString(indent));
                    }
                    break;
                case '"':
                    if (!inEscape) {
                        inString = !inString;
                    }
                    break;
            }
            if (inEscape) {
                inEscape = false;
            } else if (c == '\\') {
                inEscape = true;
            }
        }
        return result.toString();
    }

    private String indentString(int level) {
        if (level <= 0) {
            return "";
        }
        return "    ".repeat(level);
    }

}
