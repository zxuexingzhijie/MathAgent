package com.mathagent.exception;

/**
 * 提示词处理异常
 * 
 * @author Makoto
 */
public class PromptProcessingException extends MathAgentException {
    
    private final String promptName;
    
    public PromptProcessingException(String promptName, String message) {
        super("PROMPT_PROCESSING_ERROR", message);
        this.promptName = promptName;
    }
    
    public PromptProcessingException(String promptName, String message, Throwable cause) {
        super("PROMPT_PROCESSING_ERROR", message, cause);
        this.promptName = promptName;
    }
    
    public String getPromptName() {
        return promptName;
    }
}
