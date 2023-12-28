package com.mqttinsight.scripting;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.caoccao.javet.enums.V8ValueReferenceType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.mqttinsight.mqtt.MessageType;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author ptma
 */
@Slf4j
public class MqttMessageWrapper {

    private ReceivedMqttMessage message;

    public static MqttMessageWrapper of(ReceivedMqttMessage message) {
        return new MqttMessageWrapper(message);
    }

    private MqttMessageWrapper(ReceivedMqttMessage message) {
        this.message = message;
    }

    public String getTopic() {
        return message.getTopic();
    }

    public int getQos() {
        return message.getQos();
    }

    public boolean isRetained() {
        return message.isRetained();
    }

    public boolean isDuplicate() {
        return message.isDuplicate();
    }

    public long getTimestamp() {
        return message.getTimestamp();
    }

    public byte[] getPayload() {
        return message.payloadAsBytes();
    }

    public String payloadAsString() {
        return message.getPayload();
    }

    public void setPayload(String payload) {
        if (payload != null) {
            message.setPayload(payload.getBytes(StandardCharsets.UTF_8));
            message.setMessageType(MessageType.RECEIVED_SCRIPT);
        }
    }

    public void setPayload(V8ValueTypedArray payload) throws JavetException {
        if (payload.getType() == V8ValueReferenceType.Uint8Array) {
            JSONObject json = JSONUtil.parseObj(payload.toJsonString());
            if ("Buffer".equals(json.getStr("type"))) {
                message.setPayload(json.getBytes("data"));
                message.setMessageType(MessageType.RECEIVED_SCRIPT);
            } else {
                message.setPayload(payload.toBytes());
                message.setMessageType(MessageType.RECEIVED_SCRIPT);
            }
        } else if (payload.getType() == V8ValueReferenceType.Int8Array) {
            message.setPayload(payload.toBytes());
            message.setMessageType(MessageType.RECEIVED_SCRIPT);
        } else {
            log.warn("The type of the payload parameter \"{}\" is not supported.", payload.getType());
        }
    }
}
