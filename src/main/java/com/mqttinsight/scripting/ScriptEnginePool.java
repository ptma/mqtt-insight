package com.mqttinsight.scripting;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.interop.NodeRuntime;
import com.caoccao.javet.interop.converters.IJavetConverter;
import com.caoccao.javet.interop.converters.JavetProxyConverter;
import com.caoccao.javet.interop.engine.IJavetEngine;
import com.caoccao.javet.interop.engine.IJavetEnginePool;
import com.caoccao.javet.interop.engine.JavetEnginePool;
import com.mqttinsight.scripting.modules.Logger;

public class ScriptEnginePool {

    private static class ScriptEnginePoolHolder {
        private final static ScriptEnginePool INSTANCE = new ScriptEnginePool();
    }

    public static ScriptEnginePool instance() {
        return ScriptEnginePoolHolder.INSTANCE;
    }

    private final IJavetEnginePool<NodeRuntime> javetEnginePool;
    private final IJavetConverter converter;
    private final Logger logger;

    private ScriptEnginePool() {
        converter = new JavetProxyConverter();
        logger = new Logger();
        javetEnginePool = new JavetEnginePool<>();
        javetEnginePool.getConfig().setJSRuntimeType(JSRuntimeType.Node);
        javetEnginePool.getConfig().setGCBeforeEngineClose(true);
        javetEnginePool.getConfig().setAllowEval(true);
        javetEnginePool.getConfig().setJavetLogger(logger);
    }

    public ScriptEngine getScriptEngine() throws JavetException {
        IJavetEngine<NodeRuntime> javetEngine = javetEnginePool.getEngine();
        return new ScriptEngine(javetEngine, converter, logger);
    }

    public IJavetConverter getConverter() {
        return converter;
    }

    public Logger getLogger() {
        return logger;
    }

    public void close() {
        try {
            javetEnginePool.close();
        } catch (JavetException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
