package com.mqttinsight.scripting;

import cn.hutool.core.io.FileUtil;
import com.caoccao.javet.enums.V8AwaitMode;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.converters.IJavetConverter;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.executors.IV8Executor;
import com.mqttinsight.scripting.modules.CloseableModule;
import com.mqttinsight.scripting.modules.Console;
import com.mqttinsight.scripting.modules.Logger;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
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
    private CloseableModule closeableModule;
    private final File scriptFile;

    public ScriptEngine(IJavetEngine<NodeRuntime> javetEngine, IJavetConverter converter, Logger logger, File scriptFile) {
        this.javetEngine = javetEngine;
        this.converter = converter;
        this.logger = logger;
        this.scriptFile = scriptFile;
    }

    private void initRuntime() throws JavetException {
        if (this.nodeRuntime == null) {
            this.nodeRuntime = javetEngine.getV8Runtime();
            nodeRuntime.setConverter(converter);
            nodeRuntime.allowEval(false);
            nodeRuntime.setLogger(logger);
            nodeRuntime.setGCScheduled(true);
            nodeRuntime.lowMemoryNotification();

            Console console = new Console(nodeRuntime, logger);
            console.register(nodeRuntime.getGlobalObject());
        }
    }

    public void execute(Map<String, Object> modules, Consumer<ScriptResult> resultConsumer) {
        try {
            initRuntime();
            String scriptPath = scriptFile.getAbsolutePath();
            String scriptContent = FileUtil.readUtf8String(scriptFile);

            for (Map.Entry<String, Object> entry : modules.entrySet()) {
                nodeRuntime.getGlobalObject().set(entry.getKey(), entry.getValue());
                if (entry.getValue() instanceof CloseableModule) {
                    this.closeableModule = (CloseableModule) entry.getValue();
                }
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

    public void release() {
        try {
            if (closeableModule != null) {
                closeableModule.close();
            }
            if (nodeRuntime == null) {
                return;
            }
            nodeRuntime.await();
            nodeRuntime.setStopping(true);
            nodeRuntime.close();
            ScriptEnginePool.instance().releaseEngine(javetEngine);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

}
