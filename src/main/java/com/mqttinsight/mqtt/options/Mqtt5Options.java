package com.mqttinsight.mqtt.options;

import cn.hutool.core.util.StrUtil;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.security.SecureSocketFactoryBuilder;
import lombok.SneakyThrows;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author ptma
 */
public class Mqtt5Options {

    @SneakyThrows
    public static MqttConnectionOptions fromProperties(MqttProperties properties) {
        MqttConnectionOptions connOpts = new MqttConnectionOptions();

        connOpts.setConnectionTimeout(properties.getConnectionTimeout());
        connOpts.setKeepAliveInterval(properties.getKeepAliveInterval());
        connOpts.setAutomaticReconnect(properties.getReconnection().isEnable());
        if (properties.getReconnection().isEnable()) {
            connOpts.setMaxReconnectDelay(properties.getReconnection().getReconnectInterval());
        }
        connOpts.setCleanStart(properties.isCleanStart());
        if (properties.isCleanStart() && properties.getSessionExpiryInterval() != null) {
            connOpts.setSessionExpiryInterval(properties.getSessionExpiryInterval());
        }
        if (properties.getReceiveMaximum() != null) {
            connOpts.setReceiveMaximum(properties.getReceiveMaximum());
        }
        if (properties.getMaximumPacketSize() != null) {
            connOpts.setMaximumPacketSize(properties.getMaximumPacketSize());
        }
        if (properties.getTopicAliasMaximum() != null) {
            connOpts.setTopicAliasMaximum(properties.getTopicAliasMaximum());
        }
        connOpts.setRequestResponseInfo(properties.isRequestResponseInfo());
        connOpts.setRequestProblemInfo(properties.isRequestProblemInfo());
        if (properties.getUserProperties() != null) {
            connOpts.setUserProperties(properties.getUserProperties().stream().map(Property::toUserProperty).toList());
        }

        if (StrUtil.isNotEmpty(properties.getUsername()) && StrUtil.isNotEmpty(properties.getPassword())) {
            connOpts.setUserName(properties.getUsername());
            connOpts.setPassword(properties.getPassword().getBytes(StandardCharsets.UTF_8));
        }
        if (properties.getLastWill() != null && properties.getLastWill().isEnable()) {
            WillMessage willMessage = properties.getLastWill();
            connOpts.setWill(willMessage.getTopic(),
                new MqttMessage(
                    CodecSupports.instance()
                        .getByName(willMessage.getPayloadFormat())
                        .toPayload(willMessage.getTopic(), willMessage.getPayload()),
                    willMessage.getQos(),
                    willMessage.isRetained(), null)
            );
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
