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
import java.util.function.Consumer;
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

    public void decode(ReceivedMqttMessage receivedMessage, Consumer<MqttMessage> decodedConsumer) {
        if (!decodersGroupMap.isEmpty()) {
            SimpleMqttMessage mqttMessage = new SimpleMqttMessage(
                receivedMessage.getTopic(),
                receivedMessage.payloadAsBytes(),
                receivedMessage.getQos(),
                receivedMessage.isRetained(),
                receivedMessage.isDuplicate()
            );
            DecoderContext context = new DecoderContext(receivedMessage.getSubscription(), mqttMessage);
            decodersGroupMap.values().forEach(decodersMap -> {
                decodersMap.forEach((key, decoder) -> {
                    try {
                        if ("*".equals(key) || TopicUtil.match(key, context.getMessage().getTopic())) {
                            MqttMessage returnMessage = convert(context, decoder.apply(context.getMessage()));
                            if (returnMessage != null) {
                                decodedConsumer.accept(returnMessage);
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

    public void decode(String scriptPath, Function<SimpleMqttMessage, Object> scriptingDecoder) {
        decode(scriptPath, "*", scriptingDecoder);
    }

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
        if (data == null) {
            return null;
        }
        DecodedMqttMessage msg = DecodedMqttMessage.of(context.getSubscription(), context.getMessage());
        if (data instanceof String) {
            msg.setPayload(((String) data).getBytes());
        } else if (data instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) data;
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
            if (map.containsKey("color")) {
                String color = (String) map.get("color");
                if (color.matches("^#[0-9A-F]{6}$")) {
                    msg.setColor(color);
                }
            }
        } else {
            msg.setPayload(JSONUtil.toJsonStr(convertToJavaObject(data)).getBytes());
            msg.setFormat(CodecSupport.JSON);
        }
        return msg;
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
