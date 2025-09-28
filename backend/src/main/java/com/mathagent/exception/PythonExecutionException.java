package com.mathagent.exception;

/**
 * Python代码执行异常
 * 
 * @author Makoto
 */
public class PythonExecutionException extends MathAgentException {
    
    private final String sessionId;
    private final String code;
    
    public PythonExecutionException(String sessionId, String code, String message) {
        super("PYTHON_EXECUTION_ERROR", message);
        this.sessionId = sessionId;
        this.code = code;
    }
    
    public PythonExecutionException(String sessionId, String code, String message, Throwable cause) {
        super("PYTHON_EXECUTION_ERROR", message, cause);
        this.sessionId = sessionId;
        this.code = code;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public String getCode() {
        return code;
    }
}
