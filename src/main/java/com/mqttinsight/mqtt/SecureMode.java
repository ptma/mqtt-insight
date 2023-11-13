package com.mqttinsight.mqtt;

import com.mqttinsight.ui.component.Textable;
import com.mqttinsight.util.LangUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ptma
 */
@Getter
public enum SecureMode implements Textable {

    BASIC("SecureMode.basic", "SslModeBasic"),
    // Server only - cert / trust store
    SERVER_ONLY("SecureMode.serverOnly", "SslModeServerOnly"),
    SERVER_KEYSTORE("SecureMode.serverKeystore", "SslModeServerKeystore"),
    // Server and client
    SERVER_AND_CLIENT("SecureMode.serverAndClient", "SslModeServerAndClient"),
    SERVER_AND_CLIENT_KEYSTORES("SecureMode.serverAndClientKeystores", "SslModeServerAndClientKeystore"),

    PROPERTIES("SecureMode.properties", "SslModeProperties");

    private final String key;

    private final String lngKey;

    SecureMode(String key, String lngKey) {
        this.key = key;
        this.lngKey = lngKey;
    }

    @Override
    public String getText() {
        return LangUtil.getString(lngKey);
    }
}
