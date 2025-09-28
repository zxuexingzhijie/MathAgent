package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.mathagent.service.PromptService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.exception.PromptProcessingException;
import com.mathagent.util.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 模型构建节点 基于收集的数据构建数学模型
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class ModelBuilderNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PromptService promptService;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("开始执行模型构建节点...");

		try {
			Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
			Map<String, Object> collectedData = (Map<String, Object>) state.value("collected_data").get();

			// 使用PromptService构建模型构建提示
			String modelPrompt = promptService.getModelBuildingPrompt(problemAnalysis, collectedData);

			// 调用AI进行模型构建
			Prompt prompt = new Prompt(List.of(new UserMessage(modelPrompt)));
			String response = chatClient.prompt(prompt).call().content();

			// 解析模型定义
			Map<String, Object> modelDefinition = parseModelDefinition(response);

			log.info("模型构建完成: {}", modelDefinition.get("model_name"));

			return Map.of("model_definition", modelDefinition);

		}
		catch (PromptProcessingException e) {
			return ExceptionHandler.handlePromptException("模型构建", e);
		}
		catch (Exception e) {
			return ExceptionHandler.handleGenericException("模型构建", e);
		}
	}


	private Map<String, Object> parseModelDefinition(String response) {
		try {
			return objectMapper.readValue(response, Map.class);
		}
		catch (Exception e) {
			log.warn("无法解析AI响应为JSON，使用文本格式", e);
			return Map.of("raw_response", response, "model_name", "未命名模型", "summary", "模型构建完成，但格式解析失败");
		}
	}

}
