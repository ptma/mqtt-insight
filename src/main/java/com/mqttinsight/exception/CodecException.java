package com.mqttinsight.exception;

/**
 * @author ptma
 */
public class CodecException extends Exception {

    public CodecException(String error) {
        super(error);
    }

    public CodecException(String error, Throwable e) {
        super(error, e);
    }
}
