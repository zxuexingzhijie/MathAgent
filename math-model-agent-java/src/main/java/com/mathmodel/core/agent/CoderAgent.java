package com.mathmodel.core.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.mathmodel.core.interpreter.CodeInterpreter;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoderToWriter;
import com.mathmodel.schema.a2a.ModelerToCoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Coder Agent - Generates and executes Python code for mathematical modeling
 */
@Slf4j
public class CoderAgent extends BaseAgent {

    private static final int MAX_RETRIES = 3;
    private final PromptLoader promptLoader;
    private final CodeInterpreter codeInterpreter;

    public CoderAgent(
            String taskId,
            DashScopeChatModel chatModel,
            int maxChatTurns,
            PromptLoader promptLoader,
            CodeInterpreter codeInterpreter) {
        super(taskId, chatModel, maxChatTurns);
        this.promptLoader = promptLoader;
        this.codeInterpreter = codeInterpreter;
    }

    @Override
    public CoderToWriter run(Object input) throws Exception {
        if (!(input instanceof ModelerToCoder modelerOutput)) {
            throw new IllegalArgumentException("Input must be ModelerToCoder");
        }

        log.info("[CoderAgent] Starting code generation and execution for task: {}", taskId);

        // Initialize code interpreter
        if (!codeInterpreter.isReady()) {
            codeInterpreter.initialize();
        }

        // Add system prompt
        String systemPrompt = promptLoader.getCoderPrompt();
        appendSystemMessage(systemPrompt);

        // Add modeling results as user message
        String userMessage = buildUserMessage(modelerOutput);
        appendUserMessage(userMessage);

        int attempt = 0;
        String lastError = "";
        List<String> allImages = new ArrayList<>();
        StringBuilder allOutput = new StringBuilder();

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                log.info("[CoderAgent] Attempt {}/{}", attempt, MAX_RETRIES);

                // Generate code
                ChatResponse response = chat();
                String codeContent = extractPythonCode(response.getResult().getOutput().getText());

                if (codeContent == null || codeContent.isEmpty()) {
                    throw new IllegalArgumentException("No valid Python code generated");
                }

                log.debug("[CoderAgent] Generated code:\n{}", codeContent);

                // Execute code
                CodeInterpreter.ExecutionResult result = codeInterpreter.execute(codeContent, taskId);

                if (result.success()) {
                    log.info("[CoderAgent] Code execution successful");
                    
                    // Collect results
                    allImages.addAll(result.createdImages());
                    if (result.output() != null && !result.output().isEmpty()) {
                        allOutput.append(result.output()).append("\n");
                    }

                    // Build response
                    return CoderToWriter.builder()
                            .codeResponse(codeContent)
                            .createdImages(allImages)
                            .executionOutput(allOutput.toString())
                            .build();

                } else {
                    // Execution failed, use reflection prompt
                    lastError = result.error();
                    log.warn("[CoderAgent] Execution failed (attempt {}/{}): {}", 
                            attempt, MAX_RETRIES, lastError);

                    if (attempt >= MAX_RETRIES) {
                        log.error("[CoderAgent] Exceeded maximum retry attempts");
                        throw new RuntimeException(
                                "Code execution failed after " + MAX_RETRIES + " attempts: " + lastError
                        );
                    }

                    // Add reflection prompt
                    String reflectionPrompt = promptLoader.getReflectionPrompt(lastError, attempt);
                    appendUserMessage(reflectionPrompt);
                }

            } catch (Exception e) {
                lastError = e.getMessage();
                log.error("[CoderAgent] Error in attempt {}/{}: {}", attempt, MAX_RETRIES, lastError, e);

                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException(
                            "Code generation/execution failed after " + MAX_RETRIES + " attempts: " + lastError,
                            e
                    );
                }

                // Add error feedback
                String errorPrompt = String.format(
                        "⚠️ Previous attempt error: %s. Please fix and try again.",
                        lastError
                );
                appendUserMessage(errorPrompt);
            }
        }

        throw new RuntimeException("Unexpected flow termination");
    }

    /**
     * Build user message from modeling results
     */
    private String buildUserMessage(ModelerToCoder modelerOutput) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是数学建模的结果，请根据这些模型编写Python代码进行求解和可视化：\n\n");

        Map<String, Object> solutions = modelerOutput.getQuestionsSolution();
        if (solutions != null && !solutions.isEmpty()) {
            solutions.forEach((question, solution) -> {
                sb.append("【问题】").append(question).append("\n");
                sb.append("【解决方案】").append(solution).append("\n\n");
            });
        }

        sb.append("\n请编写完整的Python代码，包括：\n");
        sb.append("1. 必要的数据处理\n");
        sb.append("2. 模型求解\n");
        sb.append("3. 结果可视化（使用save_figure()函数保存图表）\n");
        sb.append("4. 输出关键结果\n");

        return sb.toString();
    }

    /**
     * Extract Python code from model response
     * Handles markdown code blocks
     */
    private String extractPythonCode(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // Try to extract from markdown code blocks
        String pattern = "```python\\s*\\n(.*?)```";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher m = p.matcher(content);

        if (m.find()) {
            return m.group(1).trim();
        }

        // Try generic code blocks
        pattern = "```\\s*\\n(.*?)```";
        p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
        m = p.matcher(content);

        if (m.find()) {
            return m.group(1).trim();
        }

        // If no code blocks, return as is (might be plain code)
        log.warn("[CoderAgent] No code blocks found, using raw content");
        return content.trim();
    }
}
