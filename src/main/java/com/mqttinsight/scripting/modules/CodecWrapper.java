package com.mqttinsight.scripting.modules;

import com.mqttinsight.codec.CodecSupport;
import com.mqttinsight.codec.CodecSupports;
import com.mqttinsight.scripting.ScriptCodec;
import com.mqttinsight.scripting.SimpleMqttMessage;

import java.util.function.Function;

/**
 * @author ptma
 */
public class CodecWrapper {

    private final ScriptCodec scriptCodec;
    private final String scriptPath;

    public CodecWrapper(String scriptPath, ScriptCodec scriptCodec) {
        this.scriptPath = scriptPath;
        this.scriptCodec = scriptCodec;
    }

    public void decode(Function<SimpleMqttMessage, Object> function) {
        scriptCodec.decode(scriptPath, function);
    }

    public void decode(String topic, Function<SimpleMqttMessage, Object> function) {
        scriptCodec.decode(scriptPath, topic, function);
    }

}
