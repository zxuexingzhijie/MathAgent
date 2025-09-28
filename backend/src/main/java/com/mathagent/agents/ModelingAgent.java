package com.mathagent.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.alibaba.cloud.ai.graph.action.NodeAction;
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
 * 建模手Agent 专门负责数学建模和问题分析
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class ModelingAgent implements NodeAction {

	@Autowired
	@Qualifier("modelingChatClient")
	private ChatClient modelingChatClient;

	@Autowired
	private PromptService promptService;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("建模手开始工作...");

		try {
			String problemStatement = state.value("input").get().toString();

			// 使用提示词服务构建建模分析提示
			String modelingPrompt = promptService.getModelingAnalysisPrompt(problemStatement);

			// 调用建模专用模型
			Prompt prompt = new Prompt(List.of(new UserMessage(modelingPrompt)));
			String response = modelingChatClient.prompt(prompt).call().content();

			// 解析建模结果
			Map<String, Object> modelingResult = parseModelingResult(response);

			// 返回建模结果
			return Map.of("modeling_analysis", modelingResult, "model_definition",
					modelingResult.get("model_definition"), "variables", modelingResult.get("variables"), "constraints",
					modelingResult.get("constraints"));

		}
		catch (Exception e) {
			log.error("建模手工作失败", e);
			return Map.of("error", "建模分析失败: " + e.getMessage());
		}
	}

	private Map<String, Object> parseModelingResult(String response) {
		try {
			return objectMapper.readValue(response, Map.class);
		}
		catch (Exception e) {
			log.warn("无法解析建模结果JSON，使用文本格式", e);
			return Map.of("raw_response", response, "summary", "建模分析完成，但格式解析失败");
		}
	}

}
