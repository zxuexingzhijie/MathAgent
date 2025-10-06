package com.mathmodel.core.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoordinatorToModeler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Map;

/**
 * Coordinator Agent - Analyzes and breaks down the problem into sub-questions
 */
@Slf4j
public class CoordinatorAgent extends BaseAgent {

    private static final int MAX_RETRIES = 3;
    private final PromptLoader promptLoader;

    public CoordinatorAgent(String taskId, DashScopeChatModel chatModel, int maxChatTurns, PromptLoader promptLoader) {
        super(taskId, chatModel, maxChatTurns);
        this.promptLoader = promptLoader;
    }

    @Override
    public CoordinatorToModeler run(Object input) throws Exception {
        if (!(input instanceof String quesAll)) {
            throw new IllegalArgumentException("Input must be a String (problem statement)");
        }

        log.info("[CoordinatorAgent] Starting problem analysis for task: {}", taskId);

        // Add system prompt from external file
        String systemPrompt = promptLoader.getCoordinatorPrompt();
        appendSystemMessage(systemPrompt);
        
        // Add user question
        appendUserMessage(quesAll);

        int attempt = 0;
        String lastError = "";

        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                log.info("[CoordinatorAgent] Attempt {}/{}", attempt, MAX_RETRIES);

                // Call LLM
                ChatResponse response = chat();
                String jsonStr = response.getResult().getOutput().getText();

                // Clean JSON string
                jsonStr = cleanJsonString(jsonStr);

                if (jsonStr == null || jsonStr.isEmpty()) {
                    throw new IllegalArgumentException("Returned JSON string is empty");
                }

                // Parse JSON
                Map<String, Object> result = objectMapper.readValue(jsonStr, new TypeReference<Map<String, Object>>() {});
                @SuppressWarnings("unchecked")
                Map<String, Object> questions = (Map<String, Object>) result.get("questions");
                Integer quesCount = (Integer) result.get("ques_count");

                log.info("[CoordinatorAgent] Successfully parsed {} questions", quesCount);
                log.debug("[CoordinatorAgent] Questions: {}", questions);

                return CoordinatorToModeler.builder()
                        .questions(questions)
                        .quesCount(quesCount)
                        .build();

            } catch (Exception e) {
                lastError = e.getMessage();
                log.warn("[CoordinatorAgent] Parsing failed (attempt {}/{}): {}", 
                        attempt, MAX_RETRIES, lastError);

                if (attempt >= MAX_RETRIES) {
                    log.error("[CoordinatorAgent] Exceeded maximum retry attempts");
                    throw new RuntimeException("Unable to parse model response after " + MAX_RETRIES + " attempts: " + lastError);
                }

                // Add error feedback
                String errorPrompt = String.format(
                        "⚠️ Previous response format error: %s. Please strictly output JSON format", 
                        lastError
                );
                String retrySystemPrompt = promptLoader.getCoordinatorPrompt();
                appendSystemMessage(retrySystemPrompt + "\n" + errorPrompt);
            }
        }

        throw new RuntimeException("Unexpected flow termination");
    }

    /**
     * Clean JSON string by removing markdown code blocks and control characters
     */
    private String cleanJsonString(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        
        jsonStr = jsonStr.replace("```json", "")
                        .replace("```", "")
                        .trim();
        
        // Remove control characters
        jsonStr = jsonStr.replaceAll("[\\x00-\\x1F\\x7F]", "");
        
        return jsonStr;
    }
}
