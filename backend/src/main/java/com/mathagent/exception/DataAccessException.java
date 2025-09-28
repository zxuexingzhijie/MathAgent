package com.mathagent.exception;

/**
 * 数据访问异常
 * 
 * @author Makoto
 */
public class DataAccessException extends MathAgentException {
    
    private final String operation;
    private final String entityType;
    
    public DataAccessException(String operation, String entityType, String message) {
        super("DATA_ACCESS_ERROR", message);
        this.operation = operation;
        this.entityType = entityType;
    }
    
    public DataAccessException(String operation, String entityType, String message, Throwable cause) {
        super("DATA_ACCESS_ERROR", message, cause);
        this.operation = operation;
        this.entityType = entityType;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getEntityType() {
        return entityType;
    }
}
