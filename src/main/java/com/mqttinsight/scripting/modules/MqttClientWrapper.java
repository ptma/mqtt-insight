package com.mqttinsight.scripting.modules;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.caoccao.javet.enums.V8ValueReferenceType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.scripting.ScriptCodec;
import com.mqttinsight.scripting.ScriptPubMqttMessage;
import com.mqttinsight.scripting.SimpleMqttMessage;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * @author ptma
 */
@Slf4j
public class MqttClientWrapper {

    private final MqttInstance mqttInstance;
    private final ScriptCodec scriptCodec;
    private final String scriptPath;

    public MqttClientWrapper(MqttInstance mqttInstance, ScriptCodec scriptCodec, String scriptPath) {
        this.mqttInstance = mqttInstance;
        this.scriptCodec = scriptCodec;
        this.scriptPath = scriptPath;
    }

    public void subscribe(String topic) {
        subscribe(topic, 0);
    }

    public void subscribe(String topic, int qos) {
        Subscription subscription = new Subscription(mqttInstance, topic, qos, CodecSupport.DEFAULT, Utils.generateRandomColor());
        mqttInstance.subscribe(subscription);
    }

    public void publish(String topic, String payload) {
        publish(topic, payload.getBytes(), 0, false);
    }

    public void publish(String topic, V8ValueTypedArray payload) throws JavetException {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, String payload, int qos) {
        publish(topic, payload.getBytes(), qos, false);
    }

    public void publish(String topic, V8ValueTypedArray payload, int qos) throws JavetException {
        publish(topic, payload, qos, false);
    }

    public void publish(String topic, byte[] payload, int qos) {
        publish(topic, payload, qos, false);
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        publish(topic, payload.getBytes(), qos, retained);
    }

    public void publish(String topic, V8ValueTypedArray payload, int qos, boolean retained) throws JavetException {
        byte[] bytes;
        if (payload.getType() == V8ValueReferenceType.Uint8Array) {
            JSONObject json = JSONUtil.parseObj(payload.toJsonString());
            if ("Buffer".equals(json.getStr("type"))) {
                bytes = json.getBytes("data");
            } else {
                bytes = payload.toBytes();
            }
        } else if (payload.getType() == V8ValueReferenceType.Int8Array) {
            bytes = payload.toBytes();
        } else {
            log.warn("The type of the payload parameter \"{}\" is not supported.", payload.getType());
            return;
        }
        publish(topic, bytes, qos, retained);
    }

    public void publish(String topic, byte[] payload, int qos, boolean retained) {
        mqttInstance.publishMessage(ScriptPubMqttMessage.of(
            topic,
            payload,
            qos,
            retained,
            CodecSupport.PLAIN
        ));
    }

    /**
     * Script API
     * <pre>
     * <code>
     * // Javascript
     * mqtt.decode(function (message) {
     *   // do something
     *   return {
     *     payload: "...",
     *     format: "json"
     *   };
     * });
     * </code>
     * </pre>
     */
    public void decode(Function<SimpleMqttMessage, Object> function) {
        scriptCodec.decode(scriptPath, function);
    }

    /**
     * Script API
     * <pre>
     * <code>
     * // Javascript
     * mqtt.decode("/test/#", function (message) {
     *   // do something
     *   return {
     *     payload: "...",
     *     format: "json"
     *   };
     * });
     * </code>
     * </pre>
     */
    public void decode(String topic, Function<SimpleMqttMessage, Object> function) {
        scriptCodec.decode(scriptPath, topic, function);
    }
}
