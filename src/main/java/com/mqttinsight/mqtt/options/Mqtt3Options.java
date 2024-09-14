package com.mqttinsight.mqtt.options;

import cn.hutool.core.util.StrUtil;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.security.SecureSocketFactoryBuilder;
import lombok.SneakyThrows;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Properties;

/**
 * @author ptma
 */
public class Mqtt3Options {

    @SneakyThrows
    public static MqttConnectOptions fromProperties(MqttProperties properties) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if (Version.MQTT_3_1_1.equals(properties.getVersion())) {
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        } else if (Version.MQTT_3_1.equals(properties.getVersion())) {
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        } else {
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        }
        connOpts.setCleanSession(properties.isCleanSession());
        connOpts.setConnectionTimeout(properties.getConnectionTimeout());
        connOpts.setKeepAliveInterval(properties.getKeepAliveInterval());
        connOpts.setAutomaticReconnect(properties.getReconnection().isEnable());
        if (properties.getReconnection().isEnable()) {
            connOpts.setMaxReconnectDelay(properties.getReconnection().getReconnectInterval());
        }
        if (StrUtil.isNotEmpty(properties.getUsername()) && StrUtil.isNotEmpty(properties.getPassword())) {
            connOpts.setUserName(properties.getUsername());
            connOpts.setPassword(properties.getPassword().toCharArray());
        }
        if (properties.getLastWill() != null && properties.getLastWill().isEnable()) {
            WillMessage willMessage = properties.getLastWill();
            connOpts.setWill(willMessage.getTopic(),
                CodecSupports.instance()
                    .getByName(willMessage.getPayloadFormat())
                    .toPayload(willMessage.getTopic(), willMessage.getPayload()),
                willMessage.getQos(),
                willMessage.isRetained());
        }
        // SSL
        SecureSetting secure = properties.getSecure();
        if (secure != null && secure.isEnable()) {
            if (SecureMode.PROPERTIES.equals(secure.getMode())) {
                Properties props = new Properties();
                for (final Property property : secure.getProperties()) {
                    props.put(property.getKey(), property.getValue());
                }
                connOpts.setSSLProperties(props);
            } else if (SecureMode.BASIC.equals(secure.getMode())) {
                connOpts.setSocketFactory(SecureSocketFactoryBuilder.getSocketFactory(secure.getProtocol()));
            } else if (SecureMode.SERVER_ONLY.equals(secure.getMode())) {
                connOpts.setSocketFactory(SecureSocketFactoryBuilder.getSocketFactory(
                    secure.getProtocol(),
                    secure.getServerCertificateFile()));
            } else if (SecureMode.SERVER_KEYSTORE.equals(secure.getMode())) {
                connOpts.setSocketFactory(SecureSocketFactoryBuilder.getSocketFactory(
                    secure.getProtocol(),
                    secure.getServerKeyStoreFile(),
                    secure.getServerKeyStorePassword()));
            } else if (SecureMode.SERVER_AND_CLIENT.equals(secure.getMode())) {
                connOpts.setSocketFactory(SecureSocketFactoryBuilder.getSocketFactory(
                    secure.getProtocol(),
                    secure.getServerCertificateFile(),
                    secure.getClientCertificateFile(),
                    secure.getClientKeyFile(),
                    secure.getClientKeyPassword(),
                    Boolean.TRUE.equals(secure.isClientKeyPEM())));
            } else if (SecureMode.SERVER_AND_CLIENT_KEYSTORES.equals(secure.getMode())) {
                connOpts.setSocketFactory(SecureSocketFactoryBuilder.getSocketFactory(
                    secure.getProtocol(),
                    secure.getServerKeyStoreFile(),
                    secure.getServerKeyStorePassword(),
                    secure.getClientKeyStoreFile(),
                    secure.getClientKeyStorePassword(),
                    secure.getClientKeyPassword()));
            }
        }

        return connOpts;
    }
}
