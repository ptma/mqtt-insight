package com.mqttinsight.ui.form.panel;

import cn.hutool.core.util.ArrayUtil;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.mqtt.*;
import com.mqttinsight.mqtt.options.Mqtt3Options;
import com.mqttinsight.util.Utils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.util.Collections;

/**
 * MQTT3 客户端实例组件
 *
 * @author ptma
 */
@Slf4j
public class Mqtt3InstanceTabPanel extends MqttInstanceTabPanel {

    protected MqttAsyncClient mqttClient;

    public Mqtt3InstanceTabPanel(MqttProperties properties) {
        super(properties);
    }

    public static Mqtt3InstanceTabPanel newInstance(MqttProperties properties) {
        return new Mqtt3InstanceTabPanel(properties);
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
        mqttClient.setCallback(new Mqtt3CallbackHandler());
    }

    @Override
    @SneakyThrows
    public void connect() {
        if (mqttClient.isConnected()) {
            mqttClient.disconnect();
        }
        mqttClient.connect(Mqtt3Options.fromProperties(properties),
            Collections.EMPTY_MAP,
            new Mqtt3ActionHandler()
        );
    }

    @Override
    @SneakyThrows
    public void disconnect() {
        if (mqttClient.isConnected()) {
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
            boolean successed = token.getException() == null;
            if (successed) {
                log.info("Subscribe topic successed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
            } else {
                log.warn("Subscribe topic failed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
            }
            return successed;
        } catch (MqttException e) {
            Utils.Toast.error(e.getMessage());
            log.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unsubscribe(final Subscription subscription) {
        try {
            IMqttToken token = mqttClient.unsubscribe(subscription.getTopic());
            boolean successed = token.getException() == null;
            if (successed) {
                log.info("Unsubscribe topic successed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos());
            } else {
                log.warn("Unsubscribe topic failed. Topic: {}, QoS: {}.", subscription.getTopic(), subscription.getQos(), token.getException());
            }
            return successed;
        } catch (MqttException e) {
            Utils.Toast.error(e.getMessage());
            log.error(e.getMessage(), e);
            return false;
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
        } catch (Exception e) {
            Utils.Toast.error(e.getMessage());
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private class Mqtt3ActionHandler implements IMqttActionListener {
        @Override
        public void onSuccess(IMqttToken token) {
            Mqtt3InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.CONNECTED);
            log.info("Connect to {} successed.", properties.completeServerURI());
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable cause) {
            MqttException ex = (MqttException) cause;
            Mqtt3InstanceTabPanel.this.onConnectionFailed(ex.getReasonCode(), ex.getMessage());
            log.warn("Connect to {} failed: {}, code: {}.", properties.completeServerURI(), ex.getMessage(), ex.getReasonCode());
        }
    }

    private class Mqtt3CallbackHandler implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            MqttException ex = (MqttException) cause;
            Mqtt3InstanceTabPanel.this.onConnectionChanged(ConnectionStatus.FAILED, ex.getReasonCode(), ex.getMessage());
            log.warn("Disconnect with error from {}, code: {}.", properties.completeServerURI(), ex.getReasonCode(), ex);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            log.debug("messageArrived: topic: {}.", topic);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            log.debug("deliveryComplete: topics: {}.", ArrayUtil.join(token.getTopics(), ","));
        }
    }

}
