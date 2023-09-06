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

/**
 * @author ptma
 */
@Slf4j
public class ScriptEngine {

    private IJavetEngine<NodeRuntime> javetEngine;
    private Logger logger;
    private NodeRuntime nodeRuntime;
    private boolean running = false;
    private boolean closing = false;

    public ScriptEngine(IJavetEngine<NodeRuntime> javetEngine, IJavetConverter converter, Logger logger) throws JavetException {
        this.javetEngine = javetEngine;
        this.logger = logger;
        this.nodeRuntime = javetEngine.getV8Runtime();
        nodeRuntime.setConverter(converter);
        nodeRuntime.allowEval(true);
        nodeRuntime.setLogger(logger);
        nodeRuntime.setPurgeEventLoopBeforeClose(true);

        Console console = new Console(nodeRuntime, logger);
        console.register(nodeRuntime.getGlobalObject());
    }

    public void execute(String scriptPath, String scriptContent, Map<String, Object> param, ResultListener resultListener) {
        try {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                nodeRuntime.getGlobalObject().set(entry.getKey(), entry.getValue());
            }
            String warpedScript = String.format("(function(){\n%s\n})();", scriptContent);
            IV8Executor executor = nodeRuntime.getExecutor(warpedScript).setResourceName(scriptPath);
            executor.executeVoid();
            if (resultListener != null) {
                resultListener.onResult(ScriptResult.success());
            }
            running = true;

            while (running && !closing) {
                nodeRuntime.await(V8AwaitMode.RunOnce);
            }

            nodeRuntime.terminateExecution();
            nodeRuntime.lowMemoryNotification();
            nodeRuntime.close(true);
            javetEngine.close();
        } catch (JavetException e) {
            if (resultListener != null) {
                resultListener.onResult(ScriptResult.error(e));
            }
        }
    }

    public void close() {
        closing = true;
    }
}
