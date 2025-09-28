package com.mathagent.agents;

import com.alibaba.cloud.ai.chat.ChatClient;
import com.alibaba.cloud.ai.chat.messages.AssistantMessage;
import com.alibaba.cloud.ai.chat.messages.UserMessage;
import com.alibaba.cloud.ai.chat.prompt.Prompt;
import com.alibaba.cloud.ai.graph.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 建模手Agent
 * 专门负责数学建模和问题分析
 */
@Slf4j
@Component
public class ModelingAgent implements NodeAction {

    @Autowired
    @Qualifier("modelingChatModel")
    private ChatClient modelingChatClient;

    @Autowired
    private PromptService promptService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("建模手开始工作...");
        
        try {
            String problemStatement = state.value("input").get().toString();
            
            // 使用提示词服务构建建模分析提示
            String modelingPrompt = promptService.getModelingAnalysisPrompt(problemStatement);
            
            // 调用建模专用模型
            Prompt prompt = new Prompt(List.of(new UserMessage(modelingPrompt)));
            AssistantMessage response = modelingChatClient.prompt(prompt).call().content();
            
            // 解析建模结果
            Map<String, Object> modelingResult = parseModelingResult(response.getContent());
            
            // 更新状态
            state.putValue("modeling_analysis", modelingResult);
            state.putValue("model_definition", modelingResult.get("model_definition"));
            state.putValue("variables", modelingResult.get("variables"));
            state.putValue("constraints", modelingResult.get("constraints"));
            
            log.info("建模手完成工作: {}", modelingResult.get("summary"));
            
        } catch (Exception e) {
            log.error("建模手工作失败", e);
            state.putValue("error", "建模分析失败: " + e.getMessage());
        }
        
        return state;
    }

    private Map<String, Object> parseModelingResult(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("无法解析建模结果JSON，使用文本格式", e);
            return Map.of(
                "raw_response", response,
                "summary", "建模分析完成，但格式解析失败"
            );
        }
    }
}
