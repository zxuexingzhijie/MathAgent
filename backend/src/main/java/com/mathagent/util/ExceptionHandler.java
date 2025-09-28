package com.mathagent.util;

import com.mathagent.exception.AgentExecutionException;
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
