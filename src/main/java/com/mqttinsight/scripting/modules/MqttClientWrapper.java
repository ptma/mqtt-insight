package com.mqttinsight.scripting.modules;

import com.caoccao.javet.enums.V8ValueReferenceType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.values.reference.V8ValueTypedArray;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.Subscription;
import com.mqttinsight.scripting.MqttMessageWrapper;
import com.mqttinsight.scripting.ScriptCodec;
import com.mqttinsight.scripting.ScriptPubMqttMessage;
import com.mqttinsight.ui.form.panel.MqttInstance;
import com.mqttinsight.util.TopicUtil;
import com.mqttinsight.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
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

    public void publish(String topic, V8ValueTypedArray payload) throws JavetException, IOException {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, byte[] payload) {
        publish(topic, payload, 0, false);
    }

    public void publish(String topic, String payload, int qos) {
        publish(topic, payload.getBytes(), qos, false);
    }

    public void publish(String topic, V8ValueTypedArray payload, int qos) throws JavetException, IOException {
        publish(topic, payload, qos, false);
    }

    public void publish(String topic, byte[] payload, int qos) {
        publish(topic, payload, qos, false);
    }

    public void publish(String topic, String payload, int qos, boolean retained) {
        publish(topic, payload.getBytes(), qos, retained);
    }

    public void publish(String topic, V8ValueTypedArray payload, int qos, boolean retained) throws JavetException, IOException {
        byte[] bytes;
        if (payload.getType() == V8ValueReferenceType.Uint8Array) {
            ObjectNode json = Utils.JSON.toObject(payload.toJsonString(), ObjectNode.class);
            if (json.get("type") != null && "Buffer".equals(json.get("type").asText())) {
                bytes = json.get("data").binaryValue();
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
     * 根据模版从 topic 上提取变量,如果提取出错则返回空Map
     *
     * <pre>
     *   topicVariables("/device/{product}","/device/test123");
     *   => {"product","test1234"}
     * </pre>
     *
     * @param template Topic模版
     * @param topic    要提取的 topic
     * @return 变量提取结果集
     */
    public Map<String, String> topicVariables(String template, String topic) {
        return TopicUtil.topicVariables(template, topic);
    }

    /**
     * 消息主题是否与订阅主题匹配
     *
     * @param pattern 订阅的主题，支持通配符
     * @param topic   发布的消息主题
     * @return 是否匹配
     */
    public boolean topicMatch(String pattern, String topic) {
        return TopicUtil.match(pattern, topic);
    }

    /**
     * 解码消息(所有主题)
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
    public void decode(Function<MqttMessageWrapper, Object> function) {
        scriptCodec.decode(scriptPath, function);
    }

    /**
     * 解码特定主主题的消息
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
    public void decode(String topic, Function<MqttMessageWrapper, Object> function) {
        scriptCodec.decode(scriptPath, topic, function);
    }

}
