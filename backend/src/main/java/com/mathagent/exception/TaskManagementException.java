package com.mathagent.exception;

/**
 * 任务管理异常
 * 
 * @author Makoto
 */
public class TaskManagementException extends MathAgentException {
    
    private final Long taskId;
    
    public TaskManagementException(Long taskId, String message) {
        super("TASK_MANAGEMENT_ERROR", message);
        this.taskId = taskId;
    }
    
    public TaskManagementException(Long taskId, String message, Throwable cause) {
        super("TASK_MANAGEMENT_ERROR", message, cause);
        this.taskId = taskId;
    }
    
    public Long getTaskId() {
        return taskId;
    }
}
