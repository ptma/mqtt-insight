package com.mqttinsight.mqtt.security;

public enum KeyStoreTypeEnum {

    DEFAULT("DEFAULT"),
    JKS("JKS"),
    JCEKS("JCEKS"),
    PKCS_12("PKCS12"),
    BKS("BKS");
    private final String value;

    KeyStoreTypeEnum(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static KeyStoreTypeEnum fromValue(String v) {
        for (KeyStoreTypeEnum c: KeyStoreTypeEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
