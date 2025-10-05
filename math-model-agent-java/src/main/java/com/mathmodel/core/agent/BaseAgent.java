package com.mathmodel.core.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * Base Agent Class
 */
@Slf4j
@Getter
public abstract class BaseAgent {

    protected final String taskId;
    protected final DashScopeChatModel chatModel;
    protected final int maxChatTurns;
    protected final List<Message> chatHistory;
    protected int currentChatTurns;
    protected final ObjectMapper objectMapper;

    public BaseAgent(String taskId, DashScopeChatModel chatModel, int maxChatTurns) {
        this.taskId = taskId;
        this.chatModel = chatModel;
        this.maxChatTurns = maxChatTurns;
        this.chatHistory = new ArrayList<>();
        this.currentChatTurns = 0;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Append message to chat history
     */
    protected void appendChatHistory(Message message) {
        chatHistory.add(message);
        log.debug("[{}] Appended message to chat history: {}", 
                this.getClass().getSimpleName(), message.getMessageType());
    }

    /**
     * Append system message
     */
    protected void appendSystemMessage(String content) {
        appendChatHistory(new SystemMessage(content));
    }

    /**
     * Append user message
     */
    protected void appendUserMessage(String content) {
        appendChatHistory(new UserMessage(content));
    }

    /**
     * Execute chat with model
     */
    protected ChatResponse chat() {
        currentChatTurns++;
        log.info("[{}] Chat turn: {}/{}", 
                this.getClass().getSimpleName(), currentChatTurns, maxChatTurns);
        
        if (currentChatTurns > maxChatTurns) {
            throw new RuntimeException(
                    String.format("Reached maximum number of chat turns (%d). Task incomplete.", maxChatTurns)
            );
        }

        Prompt prompt = new Prompt(chatHistory);
        ChatResponse response = chatModel.call(prompt);
        
        log.debug("[{}] Received response from model", this.getClass().getSimpleName());
        return response;
    }

    /**
     * Get agent name
     */
    public String getAgentName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Reset chat history
     */
    protected void resetChatHistory() {
        chatHistory.clear();
        currentChatTurns = 0;
        log.info("[{}] Chat history reset", this.getClass().getSimpleName());
    }

    /**
     * Abstract run method - to be implemented by subclasses
     */
    public abstract Object run(Object input) throws Exception;
}
