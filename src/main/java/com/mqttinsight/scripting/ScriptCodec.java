package com.mqttinsight.scripting;

import cn.hutool.json.JSONUtil;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.exceptions.JavetExecutionException;
import com.caoccao.javet.values.V8Value;
import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.util.TopicUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author ptma
 */
public class ScriptCodec {

    /**
     * Map<scriptPath, Map<topic, Function>>
     */
    private final Map<String, Map<String, Function<SimpleMqttMessage, Object>>> decodersGroupMap = new ConcurrentHashMap<>();

    public ScriptCodec() {
    }

    public void decode(ReceivedMqttMessage receivedMessage, DecoderCallback callback) {
        if (!decodersGroupMap.isEmpty()) {
            SimpleMqttMessage mqttMessage = new SimpleMqttMessage(
                receivedMessage.getTopic(),
                receivedMessage.payloadAsBytes(),
                receivedMessage.getQos(),
                receivedMessage.isRetained()
            );
            DecoderContext context = new DecoderContext(receivedMessage.getSubscription(), mqttMessage);
            decodersGroupMap.values().forEach(decodersMap -> {
                decodersMap.forEach((key, decoder) -> {
                    try {
                        if ("*".equals(key) || TopicUtil.match(key, context.getMessage().getTopic())) {
                            MqttMessage returnMessage = convert(context, decoder.apply(context.getMessage()));
                            if (returnMessage != null) {
                                callback.onDecoderResult(returnMessage);
                            }
                        }
                    } catch (Exception e) {
                        Throwable throwable = e;
                        if (e.getCause() != null) {
                            throwable = e.getCause();
                        }
                        if (throwable instanceof JavetExecutionException) {
                            ScriptEnginePool.instance().getLogger().error(((JavetExecutionException) throwable).getScriptingError().toString());
                        } else {
                            ScriptEnginePool.instance().getLogger().error(throwable.getMessage(), throwable);
                        }
                    }
                });
            });
        }
    }

    /**
     * Script API
     * <pre>
     * <code>
     * // Javascript
     * codec.decode(function (context) {
     *   var message = context.getMessage();
     *   return {
     *     payload: "...",
     *     format: "json"
     *   };
     * });
     * </code>
     * </pre>
     *
     * @param scriptingDecoder
     */
    public void decode(String scriptPath, Function<SimpleMqttMessage, Object> scriptingDecoder) {
        decode(scriptPath, "*", scriptingDecoder);
    }

    /**
     * Script API
     * <pre>
     * <code>
     * // Javascript
     * codec.decode("/test/#", function (context) {
     *   var message = context.getMessage();
     *   return {
     *     payload: "...",
     *     format: "json"
     *   };
     * });
     * </code>
     * </pre>
     *
     * @param scriptingDecoder
     */
    public void decode(String scriptPath, String topic, Function<SimpleMqttMessage, Object> scriptingDecoder) {
        if (decodersGroupMap.containsKey(scriptPath)) {
            decodersGroupMap.get(scriptPath).put(topic, scriptingDecoder);
        } else {
            Map<String, Function<SimpleMqttMessage, Object>> decodersMap = new ConcurrentHashMap<>();
            decodersMap.put(topic, scriptingDecoder);
            decodersGroupMap.put(scriptPath, decodersMap);
        }
    }

    public void removeScript(String scriptPath) {
        decodersGroupMap.remove(scriptPath);
    }

    public void removeAllScripts() {
        decodersGroupMap.clear();
    }

    private MqttMessage convert(DecoderContext context, Object data) {
        if (data instanceof String) {
            DecodedMqttMessage msg = DecodedMqttMessage.copyFrom(context.getMessage());
            msg.setSubscription(context.getSubscription());
            msg.setPayload(((String) data).getBytes());
            return msg;
        } else if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;
            DecodedMqttMessage msg = DecodedMqttMessage.copyFrom(context.getMessage());
            msg.setSubscription(context.getSubscription());
            if (map.containsKey("payload")) {
                Object payload = map.get("payload");
                if (payload instanceof String) {
                    msg.setPayload(((String) payload).getBytes());
                } else {
                    msg.setPayload(JSONUtil.toJsonStr(convertToJavaObject(payload)).getBytes());
                    msg.setFormat(CodecSupport.JSON);
                }
            }
            if (map.containsKey("format")) {
                msg.setFormat((String) map.get("format"));
            }
            return msg;
        } else {
            return null;
        }
    }

    private Object convertToJavaObject(Object scriptObj) {
        if (scriptObj instanceof V8Value) {
            try {
                return ScriptEnginePool.instance().getConverter().toObject((V8Value) scriptObj);
            } catch (JavetException e) {
                ScriptEnginePool.instance().getLogger().error(e.getMessage(), e);
                return scriptObj;
            }
        } else {
            return scriptObj;
        }
    }
}
