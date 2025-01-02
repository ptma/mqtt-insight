package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.ui.form.panel.MqttInstance;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ptma
 */
@Getter
@EqualsAndHashCode
public class Subscription {

    private final MqttInstance mqttInstance;

    /**
     * 订阅的主题
     */
    private final String topic;

    /**
     * 订阅消息等级
     */
    private final int qos;

    /**
     * 显示的颜色
     */
    @Setter
    private Color color;

    /**
     * 收到的消息数量
     */
    private final AtomicInteger messageCount;

    @Setter
    private String payloadFormat;

    @Setter
    private boolean visible = true;

    public Subscription(final MqttInstance mqttInstance, final String topic, final int qos, final String payloadFormat, final Color color) {
        this.mqttInstance = mqttInstance;
        this.topic = topic;
        this.qos = qos;
        this.color = color;
        this.payloadFormat = payloadFormat;
        messageCount = new AtomicInteger(0);
    }

    public String getSelfPayloadFormat() {
        return payloadFormat == null ? CodecSupport.DEFAULT : payloadFormat;
    }

    public String getPayloadFormat() {
        return (payloadFormat == null || CodecSupport.DEFAULT.equals(payloadFormat)) ? mqttInstance.getPayloadFormat() : payloadFormat;
    }

    public void incrementMessageCount() {
        messageCount.incrementAndGet();
    }

    public void decrementMessageCount() {
        if (messageCount.get() > 0) {
            messageCount.decrementAndGet();
        }
    }

}
