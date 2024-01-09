package com.mqttinsight.codec.proto;

public class ProtoParseException extends RuntimeException {

    public ProtoParseException(String message) {
        super(message);
    }

    public ProtoParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
