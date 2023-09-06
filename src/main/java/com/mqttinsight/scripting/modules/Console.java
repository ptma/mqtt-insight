package com.mqttinsight.scripting.modules;

import cn.hutool.core.util.StrUtil;
import com.caoccao.javet.interception.logging.BaseJavetConsoleInterceptor;
import com.caoccao.javet.interop.V8Runtime;
import com.caoccao.javet.values.V8Value;

import java.util.ArrayList;
import java.util.List;


/**
 * @author ptma
 */
public class Console extends BaseJavetConsoleInterceptor {

    protected static final String EMPTY = "";
    protected static final String ERROR_FORMAT_IS_NULL = "Format is null.";
    protected static final String ERROR_FORMAT_IS_UNDEFINED = "Format is undefined.";
    protected static final String UNDEFINED = "undefined";

    private final Logger logger;

    public Console(V8Runtime v8Runtime, Logger logger) {
        super(v8Runtime);
        this.logger = logger;
    }

    @Override
    public void consoleDebug(V8Value... v8Values) {
        logger.debug(getMessage(v8Values));
    }

    @Override
    public void consoleError(V8Value... v8Values) {
        logger.error(getMessage(v8Values));
    }

    @Override
    public void consoleInfo(V8Value... v8Values) {
        logger.info(getMessage(v8Values));
    }

    @Override
    public void consoleLog(V8Value... v8Values) {
        logger.info(getMessage(v8Values));
    }

    @Override
    public void consoleTrace(V8Value... v8Values) {
        logger.trace(getMessage(v8Values));
    }

    @Override
    public void consoleWarn(V8Value... v8Values) {
        logger.warn(getMessage(v8Values));
    }

    protected String getMessage(V8Value... v8Values) {
        final int length = v8Values.length;
        if (length == 0) {
            return EMPTY;
        } else if (length == 1) {
            final V8Value v8Value = v8Values[0];
            if (v8Value == null) {
                return UNDEFINED;
            } else {
                return v8Value.toString();
            }
        } else {
            try {
                final V8Value v8Value = v8Values[0];
                if (v8Value == null) {
                    return ERROR_FORMAT_IS_UNDEFINED;
                } else {
                    String format = v8Value.toString();
                    if (format == null || format.length() == 0) {
                        return ERROR_FORMAT_IS_NULL;
                    } else {
                        List<Object> objectArgs = new ArrayList<>();
                        for (int i = 1; i < length; ++i) {
                            objectArgs.add(getV8Runtime().toObject(v8Values[i]));
                        }
                        return StrUtil.format(format, objectArgs.toArray());
                    }
                }
            } catch (Throwable t) {
                return t.getMessage();
            }
        }
    }
}
