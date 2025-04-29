package com.mqttinsight.scripting;

import com.caoccao.javet.exceptions.JavetException;
import com.mqttinsight.mqtt.MqttMessage;
import com.mqttinsight.mqtt.ReceivedMqttMessage;
import com.mqttinsight.scripting.modules.MqttClientWrapper;
import com.mqttinsight.scripting.modules.ToastWrapper;
import com.mqttinsight.ui.form.panel.MqttInstance;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author ptma
 */
@Slf4j
public class ScriptLoader {

    private static final ToastWrapper TOAST_WRAPPER = new ToastWrapper();

    private final MqttInstance mqttInstance;
    private final ScriptCodec scriptCodec;
    private final Map<String, ScriptEngine> engines = new ConcurrentHashMap<>();

    public ScriptLoader(MqttInstance mqttInstance) {
        this.mqttInstance = mqttInstance;
        this.scriptCodec = new ScriptCodec();
    }

    public boolean isScriptLoaded(String scriptPath) {
        return engines.containsKey(scriptPath);
    }

    public void loadScript(File scriptFile, Consumer<ScriptResult> resultConsumer) {
        String scriptPath = scriptFile.getAbsolutePath();
        if (engines.containsKey(scriptPath)) {
            removeScript(scriptPath);
        }
        ScriptEngine scriptEngine = null;
        try {
            scriptEngine = ScriptEnginePool.instance().createScriptEngine(scriptFile);
        } catch (JavetException e) {
            log.error(e.getMessage());
        }
        if (scriptEngine == null) {
            resultConsumer.accept(ScriptResult.error("Failed to create Node.js runtime."));
            return;
        }
        engines.put(scriptPath, scriptEngine);

        Map<String, Object> modules = new HashMap<>();
        modules.put("mqtt", new MqttClientWrapper(mqttInstance, scriptCodec, scriptPath));
        modules.put("toast", TOAST_WRAPPER);
        modules.put("logger", ScriptEnginePool.instance().getLogger());

        scriptEngine.execute(modules, resultConsumer);
    }

    public void executeDecode(ReceivedMqttMessage receivedMessage, Consumer<MqttMessage> decodedConsumer) {
        scriptCodec.executeDecode(receivedMessage, decodedConsumer);
    }

    public void removeScript(String scriptPath) {
        scriptCodec.removeScript(scriptPath);
        ScriptEngine scriptEngine = engines.get(scriptPath);
        if (scriptEngine != null) {
            scriptEngine.release();
            engines.remove(scriptPath);
        }
    }

    public void closeAll() {
        scriptCodec.removeAllScripts();
        engines.values().forEach(ScriptEngine::release);
        engines.clear();
    }
}
