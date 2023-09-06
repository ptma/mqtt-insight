package com.mqttinsight.scripting.modules;

import com.caoccao.javet.interfaces.IJavetLogger;
import org.slf4j.LoggerFactory;

/**
 * @author ptma
 */
public class Logger implements IJavetLogger {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Logger.class);

    public void trace(String message) {
        logger.trace(message);
    }

    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    public void trace(String msg, Throwable cause) {
        logger.trace(msg, cause);
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public void debug(String msg, Throwable cause) {
        logger.debug(msg, cause);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public void info(String msg, Throwable cause) {
        logger.info(msg, cause);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String message, Throwable cause) {
        logger.error(message, cause);
    }

}
