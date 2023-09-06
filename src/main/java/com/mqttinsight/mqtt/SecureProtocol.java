package com.mqttinsight.mqtt;

import com.mqttinsight.ui.component.Textable;
import lombok.Getter;

/**
 * @author ptma
 */
@Getter
public enum SecureProtocol implements Textable {

    SSL_3("SSLv3", "SSL v3"),
    TLS_1("TLSv1", "TLS v1"),
    TLS_1_1("TLSv1.1", "TLS v1.1"),
    TLS_1_2("TLSv1.2", "TLS v1.2");

    private final String value;
    private final String text;

    SecureProtocol(String value, String text) {
        this.value = value;
        this.text = text;
    }

    public static SecureProtocol of(String value) {
        for (SecureProtocol c : SecureProtocol.values()) {
            if (c.value.equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
