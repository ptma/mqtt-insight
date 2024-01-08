package com.mqttinsight.scripting;

import com.caoccao.javet.enums.V8AwaitMode;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.converters.IJavetConverter;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.executors.IV8Executor;
import com.mqttinsight.scripting.modules.Console;
import com.mqttinsight.scripting.modules.Logger;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author ptma
 */
@Slf4j
public class ScriptEngine {

    private final IJavetEngine<NodeRuntime> javetEngine;
    private final IJavetConverter converter;
    private final Logger logger;
    private NodeRuntime nodeRuntime;

    public ScriptEngine(IJavetEngine<NodeRuntime> javetEngine, IJavetConverter converter, Logger logger) {
        this.javetEngine = javetEngine;
        this.converter = converter;
        this.logger = logger;
    }

    private void initRuntime() throws JavetException {
        if (this.nodeRuntime == null) {
            this.nodeRuntime = javetEngine.getV8Runtime();
            nodeRuntime.setConverter(converter);
            nodeRuntime.allowEval(true);
            nodeRuntime.setLogger(logger);
            nodeRuntime.setPurgeEventLoopBeforeClose(true);
            nodeRuntime.lowMemoryNotification();

            Console console = new Console(nodeRuntime, logger);
            console.register(nodeRuntime.getGlobalObject());
        }
    }

    public void execute(String scriptPath, String scriptContent, Map<String, Object> modules, Consumer<ScriptResult> resultConsumer) {
        try {
            initRuntime();

            for (Map.Entry<String, Object> entry : modules.entrySet()) {
                nodeRuntime.getGlobalObject().set(entry.getKey(), entry.getValue());
            }
            String warpedScript = String.format("(function(){\n%s\n})();", scriptContent);
            IV8Executor executor = nodeRuntime.getExecutor(warpedScript).setResourceName(scriptPath);
            executor.executeVoid();
            if (resultConsumer != null) {
                resultConsumer.accept(ScriptResult.success());
            }
            nodeRuntime.await(V8AwaitMode.RunTillNoMoreTasks);
        } catch (JavetException e) {
            if (resultConsumer != null) {
                resultConsumer.accept(ScriptResult.error(e));
            }
        }
    }

    public void closeRuntime() {
        if (nodeRuntime != null) {
            nodeRuntime.terminateExecution();
            nodeRuntime = null;
        }
    }

    public void dispose() {
        try {
            if (nodeRuntime != null) {
                nodeRuntime.terminateExecution();
                nodeRuntime = null;
            }
            javetEngine.close();
        } catch (Throwable t) {
            log.warn("Failed to close the Node.js runtime.", t);
        }
    }
}
