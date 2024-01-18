package com.mqttinsight.codec;

import cn.hutool.core.io.FileUtil;
import com.caoccao.javet.exceptions.JavetException;
import com.mqttinsight.config.Configuration;
import com.mqttinsight.scripting.ScriptEngine;
import com.mqttinsight.scripting.ScriptEnginePool;
import com.mqttinsight.scripting.modules.CodecWrapper;
import com.mqttinsight.scripting.modules.ToastWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ptma
 */
@Slf4j
public class CodecSupportLoader {

    private static final ToastWrapper TOAST_WRAPPER = new ToastWrapper();

    private static final Map<String, ScriptEngine> engines = new ConcurrentHashMap<>();

    public static void loadCodecs() {
        ServiceLoader<CodecSupport> loader = ServiceLoader.load(CodecSupport.class, CodecSupportLoader.class.getClassLoader());
        loader.iterator().forEachRemaining(provider -> {
            CodecSupports.instance().register(provider);
        });

        loadScriptingCodecs();

        Configuration.instance().getDynamicCodecs().forEach(dynamicCodec -> {
            try {
                DynamicCodecSupport codecSupport = CodecSupports.instance().getDynamicByName(dynamicCodec.getType());
                if (codecSupport == null) {
                    log.error("Cannot find the codec support {}", dynamicCodec.getType());
                } else {
                    if (FileUtil.exist(dynamicCodec.getSchemaFile())) {
                        CodecSupports.instance().register(codecSupport.newDynamicInstance(dynamicCodec.getName(), dynamicCodec.getSchemaFile()));
                    } else {
                        log.warn("The schema file \"{}\" of dynamic codec \"{}\" does not exist.", dynamicCodec.getSchemaFile(), dynamicCodec.getName());
                    }
                }
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
        });

    }

    private static void loadScriptingCodecs() {
        FileUtil.loopFiles(new File(Configuration.instance().getCodecsPath()), 1, (f) ->
            f.getName().endsWith(".js")
        ).forEach(scriptFile -> {
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
                log.error("Failed to create Node.js runtime.");
                return;
            }

            Map<String, Object> modules = new HashMap<>();
            modules.put("codec", new CodecWrapper());
            modules.put("toast", TOAST_WRAPPER);
            modules.put("logger", ScriptEnginePool.instance().getLogger());

            scriptEngine.execute(scriptPath, scriptContent, modules, t -> {
                if (!t.isSuccess()) {
                    log.error("Failed to load scripting codec '{}'. {}", scriptFile.getName(), t.getMessage(), t.getException());
                }
            });
        });
    }

    public static void dispose() {
        engines.values().forEach(ScriptEngine::dispose);
        engines.clear();
    }

}
