package com.mqttinsight.codec.proto;

public interface Element {

    default void appendIndented(StringBuilder builder, String value) {
        for (String line : value.split("\n")) {
            builder.append("  ").append(line).append('\n');
        }
    }

    static <T> T checkNotNull(T value, String name) {
        if (value == null) {
            throw new ProtoParseException(name + " == null");
        }
        return value;
    }

    static void checkArgument(boolean condition, String message, Object... messageArgs) {
        if (!condition) {
            if (messageArgs.length > 0) {
                message = String.format(message, messageArgs);
            }
            throw new IllegalArgumentException(message);
        }
    }

    static boolean isValidTag(int value) {
        return (value >= Proto.MIN_TAG_VALUE && value < Proto.RESERVED_TAG_VALUE_START) || (value > Proto.RESERVED_TAG_VALUE_END && value <= Proto.MAX_TAG_VALUE);
    }
}
