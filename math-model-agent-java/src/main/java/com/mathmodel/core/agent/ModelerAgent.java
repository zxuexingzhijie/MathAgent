package com.mathmodel.core.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoordinatorToModeler;
import com.mathmodel.schema.a2a.ModelerToCoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.Map;

/**
 * Modeler Agent - Creates mathematical models based on the problem analysis
 */
@Slf4j
public class ModelerAgent extends BaseAgent {

    private final PromptLoader promptLoader;

    public ModelerAgent(String taskId, DashScopeChatModel chatModel, int maxChatTurns, PromptLoader promptLoader) {
        super(taskId, chatModel, maxChatTurns);
        this.promptLoader = promptLoader;
    }

    @Override
    public ModelerToCoder run(Object input) throws Exception {
        if (!(input instanceof CoordinatorToModeler coordinatorToModeler)) {
            throw new IllegalArgumentException("Input must be CoordinatorToModeler instance");
        }

        log.info("[ModelerAgent] Starting model creation for task: {}", taskId);

        // Add system prompt from external file
        String systemPrompt = promptLoader.getModelerPrompt();
        appendSystemMessage(systemPrompt);

        // Add questions as user input
        String questionsJson = objectMapper.writeValueAsString(coordinatorToModeler.getQuestions());
        appendUserMessage(questionsJson);

        // Call LLM
        ChatResponse response = chat();
        String jsonStr = response.getResult().getOutput().getContent();

        // Clean JSON string
        jsonStr = cleanJsonString(jsonStr);

        if (jsonStr == null || jsonStr.isEmpty()) {
            throw new IllegalArgumentException("Returned JSON string is empty. Please check input content.");
        }

        try {
            // Parse JSON
            Map<String, Object> questionsSolution = objectMapper.readValue(
                    jsonStr, new TypeReference<>() {}
            );

            log.info("[ModelerAgent] Successfully created models for {} questions", 
                    questionsSolution.size());
            log.debug("[ModelerAgent] Solutions: {}", questionsSolution);

            return ModelerToCoder.builder()
                    .questionsSolution(questionsSolution)
                    .build();

        } catch (Exception e) {
            log.error("[ModelerAgent] JSON parsing error: {}", e.getMessage());
            throw new IllegalArgumentException("JSON parsing error: " + e.getMessage(), e);
        }
    }

    /**
     * Clean JSON string by removing markdown code blocks
     */
    private String cleanJsonString(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        
        return jsonStr.replace("```json", "")
                     .replace("```", "")
                     .trim();
    }
}
