package com.mqttinsight.codec.proto;

public enum Syntax {

    PROTO2("proto2"), PROTO3("proto3");

    private final String name;

    Syntax(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
