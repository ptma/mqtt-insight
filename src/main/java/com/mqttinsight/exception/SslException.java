package com.mqttinsight.exception;

/**
 * @author ptma
 */
public class SslException extends Exception {

    public SslException(String error) {
        super(error);
    }

    public SslException(String error, Throwable e) {
        super(error, e);
    }
}
