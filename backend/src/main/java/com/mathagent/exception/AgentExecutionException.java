package com.mathagent.exception;

/**
 * Agent执行异常
 * 
 * @author Makoto
 */
public class AgentExecutionException extends MathAgentException {
    
    private final String agentName;
    
    public AgentExecutionException(String agentName, String message) {
        super("AGENT_EXECUTION_ERROR", message);
        this.agentName = agentName;
    }
    
    public AgentExecutionException(String agentName, String message, Throwable cause) {
        super("AGENT_EXECUTION_ERROR", message, cause);
        this.agentName = agentName;
    }
    
    public String getAgentName() {
        return agentName;
    }
}
