package com.mqttinsight.scripting;

import com.caoccao.javet.exceptions.BaseJavetScriptingException;
import com.caoccao.javet.exceptions.JavetException;
import com.caoccao.javet.exceptions.JavetScriptingError;

/**
 * @author ptma
 */
public class ScriptResult {

    private boolean success;

    private String message;

    private transient Exception exception;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        if (message == null && exception != null) {
            message = exception.getMessage();
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public static ScriptResult success() {
        ScriptResult result = new ScriptResult();
        result.setSuccess(true);
        return result;
    }

    public static ScriptResult error(String message) {
        ScriptResult result = new ScriptResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }

    public static ScriptResult error(Exception exception) {
        if (exception instanceof JavetException) {
            return error((JavetException) exception);
        } else {
            ScriptResult result = new ScriptResult();
            result.setSuccess(false);
            result.setException(exception);
            result.setMessage(exception.getMessage());
            return result;
        }
    }

    public static ScriptResult error(JavetException exception) {
        ScriptResult result = new ScriptResult();
        result.setSuccess(false);
        if (exception instanceof BaseJavetScriptingException) {
            BaseJavetScriptingException scriptingException = (BaseJavetScriptingException) exception;
            JavetScriptingError scriptingError = scriptingException.getScriptingError();
            StringBuilder sb = new StringBuilder();
            sb.append("Resource: ").append(scriptingError.getResourceName()).append("\n");
            sb.append("Source  : ").append(scriptingError.getSourceLine()).append("\n");
            sb.append("Line    : ").append(scriptingError.getLineNumber()).append("\n");
            sb.append("Column  : ").append(scriptingError.getStartColumn()).append(", ").append(scriptingError.getEndColumn()).append("\n");
            sb.append("Stack   : ").append(scriptingError.getStack());

            result.setMessage(sb.toString());
        } else {
            result.setMessage(exception.getMessage());
            result.setException(exception);
        }
        return result;
    }
}
