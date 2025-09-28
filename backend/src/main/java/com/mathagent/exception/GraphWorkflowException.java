package com.mathagent.exception;

/**
 * Graph工作流异常
 * 
 * @author Makoto
 */
public class GraphWorkflowException extends MathAgentException {
    
    private final String nodeName;
    private final String executionId;
    
    public GraphWorkflowException(String nodeName, String executionId, String message) {
        super("GRAPH_WORKFLOW_ERROR", message);
        this.nodeName = nodeName;
        this.executionId = executionId;
    }
    
    public GraphWorkflowException(String nodeName, String executionId, String message, Throwable cause) {
        super("GRAPH_WORKFLOW_ERROR", message, cause);
        this.nodeName = nodeName;
        this.executionId = executionId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public String getExecutionId() {
        return executionId;
    }
}
