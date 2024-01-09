package com.mqttinsight.exception;

/**
 * @author ptma
 */
public class SchemaLoadException extends RuntimeException {

    public SchemaLoadException(String error) {
        super(error);
    }

    public SchemaLoadException(Throwable e) {
        super(e);
    }

    public SchemaLoadException(String error, Throwable e) {
        super(error, e);
    }
}
