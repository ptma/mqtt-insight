package com.mqttinsight.mqtt;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.ui.form.panel.MqttInstance;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Date;
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
    private Color color;

    /**
     * 收到的消息数量
     */
    private final AtomicInteger messageCount;

    private String payloadFormat;

    @Setter
    private boolean visible = true;

    /**
     * 开始订阅的时间
     */
    private final Date subscribeTime;

    public Subscription(final MqttInstance mqttInstance, final String topic, final int qos, final String payloadFormat, final Color color) {
        this.mqttInstance = mqttInstance;
        this.topic = topic;
        this.qos = qos;
        this.color = color;
        this.subscribeTime = new Date();
        this.payloadFormat = payloadFormat;
        messageCount = new AtomicInteger(0);
    }

    public void setPayloadFormat(String payloadFormat) {
        this.payloadFormat = payloadFormat;
    }

    public String getSelfPayloadFormat() {
        return payloadFormat == null ? CodecSupport.DEFAULT : payloadFormat;
    }

    public String getPayloadFormat() {
        return (payloadFormat == null || CodecSupport.DEFAULT.equals(payloadFormat)) ? mqttInstance.getPayloadFormat() : payloadFormat;
    }

    public void incrementMessageCount() {
        this.messageCount.incrementAndGet();
    }

    public void decrementMessageCount() {
        if (messageCount.get() > 0) {
            this.messageCount.decrementAndGet();
        }
    }

    public void resetMessageCount() {
        this.messageCount.set(0);
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
