package com.mathagent.exception;

/**
 * 数学建模Agent系统基础异常
 * 
 * @author Makoto
 */
public class MathAgentException extends Exception {
    
    private final String errorCode;
    
    public MathAgentException(String message) {
        super(message);
        this.errorCode = "MATH_AGENT_ERROR";
    }
    
    public MathAgentException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public MathAgentException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MATH_AGENT_ERROR";
    }
    
    public MathAgentException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
