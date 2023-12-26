package com.mqttinsight.ui.form.panel;

import cn.hutool.core.thread.ThreadUtil;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.options.Mqtt3Options;
import com.mqttinsight.util.LangUtil;
import com.mqttinsight.util.Utils;
import lombok.SneakyThrows;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.Collections;
import java.util.function.Consumer;

/**
 * MQTT3 客户端实例组件
 *
 * @author ptma
 */
public class Mqtt3InstanceTabPanel extends MqttInstanceTabPanel {

    protected MqttAsyncClient mqttClient;
    private MqttClientPersistence persistence;

    public Mqtt3InstanceTabPanel(MqttProperties properties) {
        super(properties);
    }

    public static Mqtt3InstanceTabPanel newInstance(MqttProperties properties) {
        return new Mqtt3InstanceTabPanel(properties);
    }

    @Override
    @SneakyThrows
    public void initMqttClient() {
        persistence = new MqttDefaultFilePersistence(Configuration.instance().getTempPath());
        mqttClient = new MqttAsyncClient(
            properties.completeServerURI(),
            properties.getClientId(),
            persistence
        );
        mqttClient.setCallback(new Mqtt3CallbackHandler());
    }

    @Override
    public void connect() {
        ThreadUtil.execute(() -> {
            try {
                if (mqttClient == null) {
                    initMqttClient();
                }
                onConnectionChanged(ConnectionStatus.CONNECTING);
                if (mqttClient.isConnected()) {
                    mqttClient.reconnect();
                } else {
                    mqttClient.connect(Mqtt3Options.fromProperties(properties),
                        Collections.EMPTY_MAP,
                        new Mqtt3ActionHandler()
                    );
                }
            } catch (MqttException e) {
                String causeMessage = getCauseMessage(e);
                onConnectionFailed(e.getReasonCode(), causeMessage);
                log.error(e.getMessage(), e);
                Utils.Toast.error(causeMessage);
            }
        });
    }

    @Override
    @SneakyThrows
    public void disconnect(boolean withFail) {
        if (withFail) {
            ThreadUtil.execute(() -> {
                try {
                    mqttClient.disconnectForcibly();
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            });
        } else {
            onConnectionChanged(ConnectionStatus.DISCONNECTING);
            mqttClient.disconnect();
            onConnectionChanged(ConnectionStatus.DISCONNECTED);
        }
    }

    @Override
    @SneakyThrows
    public void doClose() {
        try {
            mqttClient.close(true);
            persistence.close();
        } catch (Exception ignore) {

        }
    }

    @Override
    public boolean isConnected() {
        return mqttClient.isConnected();
    }

    @Override
    public boolean doSubscribe(final Subscription subscription) {
        try {
            IMqttToken token = mqttClient.subscribe(subscription.getTopic(), subscription.getQos(), (topic, message) -> {
                ReceivedMqttMessage mqttMessage = ReceivedMqttMessage.of(subscription,
                    topic,
                    message.getPayload(),
                    message.getQos(),
                    message.isRetained(),
                    message.isDuplicate()
                );
                subscription.incrementMessageCount();
                messageReceived(mqttMessage);
            });
            boolean success = token.getException() == null;
            if (success) {
                applyEvent(l -> l.onSubscribe(subscription));
                log.info("Successfully subscribed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
            } else {
                log.warn("Failed to subscribe. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
            }
            return success;
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
            mqttClient.unsubscribe(subscription.getTopic(),
                null,
                new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken token) {
                        unsubscribed.accept(Boolean.TRUE);
                        log.info("Successfully unsubscribed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
                    }

                    @Override
                    public void onFailure(IMqttToken token, Throwable exception) {
                        unsubscribed.accept(Boolean.FALSE);
                        log.warn("Failed to unsubscribe. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
                    }
                }
            );
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
            IMqttDeliveryToken token = mqttClient.publish(message.getTopic(),
                message.payloadAsBytes(),
                message.getQos(),
                message.isRetained(),
                null,
                new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken token) {

                    }

                    @Override
                    public void onFailure(IMqttToken token, Throwable exception) {
                        Utils.Toast.error(exception.getMessage());
                        log.warn("Failed to publish message. Topic: {}, QoS: {}, Retained: {}.", message.getTopic(), message.getQos(), message.isRetained(), exception);
                    }
                }
            );
            token.waitForCompletion(5000);
            boolean success = token.getException() == null;
            if (success) {
                log.info("Successfully published message. Topic: {}, QoS: {}, Retained: {}.", message.getTopic(), message.getQos(), message.isRetained());
            } else {
                log.warn("Failed to publish message. Topic: {}, QoS: {}, Retained: {}.", message.getTopic(), message.getQos(), message.isRetained(), token.getException());
            }
            return success;
        } catch (MqttException e) {
            String causeMessage = getCauseMessage(e);
            Utils.Toast.error(causeMessage);
            log.error(causeMessage, e);
            return false;
        }
    }

    private class Mqtt3ActionHandler implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken token) {
            Mqtt3InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.CONNECTED);
            log.info("Successfully connected to {}.", properties.completeServerURI());
        }

        @Override
        public void onFailure(IMqttToken token, Throwable cause) {
            MqttException ex = (MqttException) cause;
            String causeMessage = getCauseMessage(ex);
            Mqtt3InstanceTabPanel.this.onConnectionFailed(ex.getReasonCode(), causeMessage);
            log.warn("Failed to connect to {}. errorCode: {}, {}", properties.completeServerURI(), ex.getReasonCode(), causeMessage);
        }
    }

    private class Mqtt3CallbackHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            MqttException ex = (MqttException) cause;
            String causeMessage = getCauseMessage(ex);
            Mqtt3InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.FAILED, ex.getReasonCode(), causeMessage);
            log.warn("Disconnected from {} with an error. errorCode: {}, {}", properties.completeServerURI(), ex.getReasonCode(), causeMessage);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            //log.debug("messageArrived: topic: {}.", topic);
            ReceivedMqttMessage mqttMessage = ReceivedMqttMessage.of(null,
                topic,
                message.getPayload(),
                message.getQos(),
                message.isRetained(),
                message.isDuplicate()
            );
            mqttMessage.setFormat(properties.getPayloadFormat());
            messageReceived(mqttMessage);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            //log.debug("deliveryComplete: topics: {}.", ArrayUtil.join(token.getTopics(), ","));
        }
    }

    private String getCauseMessage(MqttException exception) {
        return LangUtil.getString("MqttReasonCode_" + exception.getReasonCode(), exception.getMessage());
    }
}
