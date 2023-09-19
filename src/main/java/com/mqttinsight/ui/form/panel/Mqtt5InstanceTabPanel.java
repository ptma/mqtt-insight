package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.options.Mqtt5Options;
import com.mqttinsight.util.Utils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttReturnCode;

import java.io.File;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;

/**
 * MQTT5 客户端实例组件
 *
 * @author ptma
 */
@Slf4j
public class Mqtt5InstanceTabPanel extends MqttInstanceTabPanel {

    protected MqttAsyncClient mqttClient;

    public Mqtt5InstanceTabPanel(MqttProperties properties) {
        super(properties);
    }

    public static Mqtt5InstanceTabPanel newInstance(MqttProperties properties) {
        return new Mqtt5InstanceTabPanel(properties);
    }

    @Override
    @SneakyThrows
    public void initMqttClient() {
        MqttClientPersistence persistence = new MqttDefaultFilePersistence(Configuration.instance().getUserPath() + File.separator + "temp");
        mqttClient = new MqttAsyncClient(
            properties.completeServerURI(),
            properties.getClientId(),
            persistence
        );
        mqttClient.setCallback(new Mqtt5CallbackHandler());
    }

    @Override
    public void connect() {
        ThreadUtil.execute(() -> {
            try {
                if (mqttClient == null) {
                    initMqttClient();
                }
                if (mqttClient.isConnected()) {
                    onConnectionChanged(ConnectionStatus.DISCONNECTING);
                    mqttClient.disconnect();
                }
                onConnectionChanged(ConnectionStatus.CONNECTING);
                mqttClient.connect(Mqtt5Options.fromProperties(properties),
                    Collections.EMPTY_MAP,
                    new Mqtt5ActionHandler()
                );
            } catch (MqttException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        if (mqttClient.isConnected()) {
            onConnectionChanged(ConnectionStatus.DISCONNECTING);
            mqttClient.disconnect();
            onConnectionChanged(ConnectionStatus.DISCONNECTED);
        }
    }

    @Override
    @SneakyThrows
    public void close() {
        super.close();
        try {
            mqttClient.close(true);
        } catch (Exception ignore) {

        }
    }

    @Override
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public boolean subscribe(final Subscription subscription) {
        try {
            org.eclipse.paho.mqttv5.common.packet.MqttProperties prop = new org.eclipse.paho.mqttv5.common.packet.MqttProperties();
            prop.setSubscriptionIdentifiers(List.of(0));
            IMqttToken token = mqttClient.subscribe(new MqttSubscription(subscription.getTopic(), subscription.getQos()),
                null,
                null,
                (topic, message) -> {
                    ReceivedMqttMessage mqttMessage = ReceivedMqttMessage.of(subscription,
                        topic,
                        message.getPayload(),
                        message.getQos(),
                        message.isRetained(),
                        message.isDuplicate()
                    );
                    subscription.incrementMessageCount();
                    messageReceived(mqttMessage);
                },
                prop);
            boolean successed = token.getException() == null;
            if (successed) {
                log.info("Subscribe topic successed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
            } else {
                log.warn("Subscribe topic failed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
            }
            return successed;
        } catch (MqttException e) {
            String causeMessage = getCauseMessage(e);
            Utils.Toast.error(causeMessage);
            log.error(causeMessage, e);
            return false;
        }
    }

    @Override
    public void unsubscribe(final Subscription subscription, Consumer<Boolean> unsubscribed) {
        try {
            MqttActionListener unsubscribeCallback = new MqttActionListener() {
                @Override
                public void onSuccess(IMqttToken token) {
                    unsubscribed.accept(Boolean.TRUE);
                    log.info("Unsubscribe topic successed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
                }

                @Override
                public void onFailure(IMqttToken token, Throwable exception) {
                    unsubscribed.accept(Boolean.FALSE);
                    log.warn("Unsubscribe topic failed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
                }
            };

            try {
                mqttClient.unsubscribe(subscription.getTopic(), null, unsubscribeCallback);
            } catch (ConcurrentModificationException e) {
                /*
                 * unsubscribe again
                 * Issues #986 https://githubfast.com/eclipse/paho.mqtt.java/issues/986
                 */
                mqttClient.unsubscribe(subscription.getTopic(), null, unsubscribeCallback);
            }
        } catch (MqttException e) {
            String causeMessage = getCauseMessage(e);
            Utils.Toast.error(causeMessage);
            log.error(causeMessage, e);
            unsubscribed.accept(Boolean.FALSE);
        }
    }

    @Override
    public boolean doPublishMessage(PublishedMqttMessage message) {
        if (!mqttClient.isConnected()) {
            Utils.Toast.info("Not connected yet!");
            return false;
        }
        try {
            IMqttToken token = mqttClient.publish(message.getTopic(),
                message.payloadAsBytes(),
                message.getQos(),
                message.isRetained(),
                null,
                new MqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Utils.Toast.error(exception.getMessage());
                        log.error(exception.getMessage(), exception);
                    }
                }
            );
            token.waitForCompletion();
            boolean successed = token.getException() == null;
            if (successed) {
                log.info("Publish message successed. Topic: {}, QoS: {}, Retained: {}.", message.getTopic(), message.getQos(), message.isRetained());
            } else {
                log.warn("Publish message failed. Topic: {}, QoS: {}, Retained: {}.", message.getTopic(), message.getQos(), message.isRetained(), token.getException());
            }
            return successed;
        } catch (MqttException e) {
            String causeMessage = getCauseMessage(e);
            Utils.Toast.error(causeMessage);
            log.error(causeMessage, e);
            return false;
        }
    }

    private class Mqtt5ActionHandler implements MqttActionListener {
        @Override
        public void onSuccess(IMqttToken token) {
            onConnectionChanged(ConnectionStatus.CONNECTED);
            log.info("Connect to {} successed.", properties.completeServerURI());
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            log.warn("Connect to {} failed.", properties.completeServerURI(), exception);
            MqttException ex = (MqttException) exception;
            String causeMessage = getCauseMessage(ex);
            onConnectionFailed(ex.getReasonCode(), causeMessage);
        }
    }

    private class Mqtt5CallbackHandler implements MqttCallback {
        @Override
        public void disconnected(MqttDisconnectResponse response) {
            if (response.getReturnCode() == MqttReturnCode.RETURN_CODE_SUCCESS
                || response.getReturnCode() == MqttReturnCode.RETURN_CODE_DISCONNECT_WITH_WILL_MESSAGE) {
                Mqtt5InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.DISCONNECTED);
                log.info("Disconnect to {} successed.", properties.completeServerURI());
            } else {
                String causeMessage = getCauseMessage(response.getException());
                Mqtt5InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.FAILED, response.getReturnCode(), causeMessage);
                log.warn("Disconnect with error from {}, code: {}.", properties.completeServerURI(), response.getReturnCode(), response.getException());
            }
        }

        @Override
        public void mqttErrorOccurred(MqttException exception) {
            log.warn("Mqtt error from {}, code: {}.", properties.completeServerURI(), exception.getReasonCode(), exception);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {

        }

        @Override
        public void deliveryComplete(IMqttToken token) {

        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            Mqtt5InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.CONNECTED);
            log.info("Connect to {} successed.", serverURI);
        }

        @Override
        public void authPacketArrived(int reasonCode, org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {

        }
    }

    private String getCauseMessage(MqttException exception) {
        String causeMessage = exception.getMessage();
        if (exception.getCause() != null) {
            causeMessage = exception.getCause().getMessage();
        }
        return causeMessage;
    }
}
