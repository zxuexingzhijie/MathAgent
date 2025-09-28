package com.mathagent.util;

import com.mathagent.exception.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 异常处理工具类
 * 
 * @author Makoto
 */
@Slf4j
public class ExceptionHandler {
    
    /**
     * 处理Agent执行异常
     */
    public static Map<String, Object> handleAgentException(String agentName, Exception e) {
        log.error("{}执行失败", agentName, e);
        
        if (e instanceof AgentExecutionException) {
            AgentExecutionException aee = (AgentExecutionException) e;
            return Map.of(
                "error", aee.getMessage(),
                "errorCode", aee.getErrorCode(),
                "agentName", aee.getAgentName()
            );
        }
        
        return Map.of(
            "error", agentName + "执行失败: " + e.getMessage(),
            "errorCode", "AGENT_EXECUTION_ERROR",
            "agentName", agentName
        );
    }
    
    /**
     * 处理Python执行异常
     */
    public static Map<String, Object> handlePythonException(String sessionId, String code, Exception e) {
        log.error("Python代码执行失败: {}", sessionId, e);
        
        return Map.of(
            "error", "Python代码执行失败: " + e.getMessage(),
            "errorCode", "PYTHON_EXECUTION_ERROR",
            "sessionId", sessionId,
            "success", false
        );
    }
    
    /**
     * 处理任务管理异常
     */
    public static Map<String, Object> handleTaskException(Long taskId, Exception e) {
        log.error("任务管理失败: {}", taskId, e);
        
        if (e instanceof TaskManagementException) {
            TaskManagementException tme = (TaskManagementException) e;
            return Map.of(
                "error", tme.getMessage(),
                "errorCode", tme.getErrorCode(),
                "taskId", tme.getTaskId()
            );
        }
        
        return Map.of(
            "error", "任务管理失败: " + e.getMessage(),
            "errorCode", "TASK_MANAGEMENT_ERROR",
            "taskId", taskId
        );
    }
    
    /**
     * 处理Graph工作流异常
     */
    public static Map<String, Object> handleGraphException(String nodeName, String executionId, Exception e) {
        log.error("Graph工作流执行失败: {} - {}", nodeName, executionId, e);
        
        if (e instanceof GraphWorkflowException) {
            GraphWorkflowException gwe = (GraphWorkflowException) e;
            return Map.of(
                "error", gwe.getMessage(),
                "errorCode", gwe.getErrorCode(),
                "nodeName", gwe.getNodeName(),
                "executionId", gwe.getExecutionId()
            );
        }
        
        return Map.of(
            "error", "Graph工作流执行失败: " + e.getMessage(),
            "errorCode", "GRAPH_WORKFLOW_ERROR",
            "nodeName", nodeName,
            "executionId", executionId
        );
    }
    
    /**
     * 处理提示词处理异常
     */
    public static Map<String, Object> handlePromptException(String promptName, Exception e) {
        log.error("提示词处理失败: {}", promptName, e);
        
        if (e instanceof PromptProcessingException) {
            PromptProcessingException ppe = (PromptProcessingException) e;
            return Map.of(
                "error", ppe.getMessage(),
                "errorCode", ppe.getErrorCode(),
                "promptName", ppe.getPromptName()
            );
        }
        
        return Map.of(
            "error", "提示词处理失败: " + e.getMessage(),
            "errorCode", "PROMPT_PROCESSING_ERROR",
            "promptName", promptName
        );
    }
    
    /**
     * 处理数据访问异常
     */
    public static Map<String, Object> handleDataAccessException(String operation, String entityType, Exception e) {
        log.error("数据访问失败: {} - {}", operation, entityType, e);
        
        if (e instanceof DataAccessException) {
            DataAccessException dae = (DataAccessException) e;
            return Map.of(
                "error", dae.getMessage(),
                "errorCode", dae.getErrorCode(),
                "operation", dae.getOperation(),
                "entityType", dae.getEntityType()
            );
        }
        
        return Map.of(
            "error", "数据访问失败: " + e.getMessage(),
            "errorCode", "DATA_ACCESS_ERROR",
            "operation", operation,
            "entityType", entityType
        );
    }
    
    /**
     * 处理通用异常
     */
    public static Map<String, Object> handleGenericException(String operation, Exception e) {
        log.error("{}失败", operation, e);
        
        return Map.of(
            "error", operation + "失败: " + e.getMessage(),
            "errorCode", "GENERIC_ERROR"
        );
    }
}
