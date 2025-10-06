package com.mathmodel.core.interpreter;

import java.util.List;

/**
 * Code Interpreter Interface
 * Defines the contract for executing Python code
 */
public interface CodeInterpreter {

    /**
     * Execute Python code
     * 
     * @param code Python code to execute
     * @param taskId Task ID for context
     * @return Execution result containing output, error, and images
     * @throws Exception if execution fails
     */
    ExecutionResult execute(String code, String taskId) throws Exception;

    /**
     * Check if the interpreter is ready
     * 
     * @return true if ready to execute code
     */
    boolean isReady();

    /**
     * Initialize the interpreter
     * 
     * @throws Exception if initialization fails
     */
    void initialize() throws Exception;

    /**
     * Shutdown the interpreter and clean up resources
     */
    void shutdown();

    /**
     * Execution Result DTO
     */
    record ExecutionResult(
            boolean success,
            String output,
            String error,
            List<String> createdImages,
            long executionTimeMs
    ) {
        public static ExecutionResult success(String output, List<String> images, long timeMs) {
            return new ExecutionResult(true, output, null, images, timeMs);
        }

        public static ExecutionResult failure(String error, long timeMs) {
            return new ExecutionResult(false, null, error, List.of(), timeMs);
        }
    }
}
