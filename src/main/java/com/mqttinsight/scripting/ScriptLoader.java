package com.mqttinsight.scripting;

import cn.hutool.core.io.FileUtil;
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

    public void loadScript(File scriptFile, Consumer<ScriptResult> resultConsumer) {
        String scriptPath = scriptFile.getAbsolutePath();
        String scriptContent = FileUtil.readUtf8String(scriptFile);
        ScriptEngine scriptEngine = engines.computeIfAbsent(scriptPath, (k) -> {
            try {
                return ScriptEnginePool.instance().getScriptEngine();
            } catch (JavetException e) {
                log.error(e.getMessage());
                return null;
            }
        });
        if (scriptEngine == null) {
            resultConsumer.accept(ScriptResult.error("Failed to create Node.js runtime."));
            return;
        }

        removeScript(scriptPath);

        Map<String, Object> modules = new HashMap<>();
        modules.put("mqtt", new MqttClientWrapper(mqttInstance, scriptCodec, scriptPath));
        modules.put("toast", TOAST_WRAPPER);
        modules.put("logger", ScriptEnginePool.instance().getLogger());

        scriptEngine.execute(scriptPath, scriptContent, modules, resultConsumer);
    }

    public void executeDecode(ReceivedMqttMessage receivedMessage, Consumer<MqttMessage> decodedConsumer) {
        scriptCodec.executeDecode(receivedMessage, decodedConsumer);
    }

    public void removeScript(String scriptPath) {
        scriptCodec.removeScript(scriptPath);
        ScriptEngine scriptEngine = engines.get(scriptPath);
        if (scriptEngine != null) {
            scriptEngine.closeRuntime();
            engines.remove(scriptPath);
        }
    }

    public void closeAll() {
        scriptCodec.removeAllScripts();
        engines.values().forEach(ScriptEngine::dispose);
        engines.clear();
    }
}
