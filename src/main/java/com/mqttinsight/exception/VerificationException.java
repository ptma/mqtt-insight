package com.mqttinsight.exception;

/**
 * @author ptma
 */
public class VerificationException extends Exception {

    public VerificationException(String error) {
        super(error);
    }

    public VerificationException(String error, Throwable e) {
        super(error, e);
    }
}
