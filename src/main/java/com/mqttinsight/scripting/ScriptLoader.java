package com.mqttinsight.scripting;

import cn.hutool.core.io.FileUtil;
import com.caoccao.javet.exceptions.JavetException;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.scripting.modules.CodecWrapper;
import com.mqttinsight.scripting.modules.MqttClientWrapper;
import com.mqttinsight.scripting.modules.ToastWrapper;
import com.mqttinsight.ui.form.panel.MqttInstance;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author ptma
 */
public class ScriptLoader {

    private static final ToastWrapper TOAST_WRAPPER = new ToastWrapper();

    private final MqttClientWrapper mqttClient;
    private final ScriptCodec scriptDecoder;
    private final Map<String, ScriptEngine> engines = new ConcurrentHashMap<>();

    public ScriptLoader(MqttInstance mqttInstance) {
        this.mqttClient = new MqttClientWrapper(mqttInstance);
        this.scriptDecoder = new ScriptCodec();
    }

    public void loadScript(File scriptFile, Consumer<ScriptResult> resultConsumer) {
        String scriptPath = scriptFile.getAbsolutePath();
        String scriptContent = FileUtil.readUtf8String(scriptFile);

        removeScript(scriptPath);

        try {
            ScriptEngine scriptEngine = ScriptEnginePool.instance().getScriptEngine();
            engines.put(scriptPath, scriptEngine);

            Map<String, Object> modules = new HashMap<>();
            modules.put("mqtt", mqttClient);
            modules.put("codec", new CodecWrapper(scriptPath, scriptDecoder));
            modules.put("toast", TOAST_WRAPPER);
            modules.put("logger", ScriptEnginePool.instance().getLogger());

            scriptEngine.execute(scriptPath, scriptContent, modules, resultConsumer);
        } catch (JavetException e) {
            engines.remove(scriptPath);
            if (resultConsumer != null) {
                resultConsumer.accept(ScriptResult.error(e));
            }
        }
    }

    public void decode(ReceivedMqttMessage receivedMessage, Consumer<MqttMessage> decodedConsumer) {
        scriptDecoder.decode(receivedMessage, decodedConsumer);
    }

    public void removeScript(String scriptPath) {
        scriptDecoder.removeScript(scriptPath);
        if (engines.containsKey(scriptPath)) {
            ScriptEngine scriptEngine = engines.get(scriptPath);
            scriptEngine.close();
            engines.remove(scriptPath);
        }
    }

    public void closeAll() {
        scriptDecoder.removeAllScripts();
        engines.values().forEach(ScriptEngine::close);
        engines.clear();
    }
}
