package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 求解器节点 执行模型求解和验证
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class SolverNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PromptService promptService;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("开始执行求解器节点...");

		try {
			Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
			Map<String, Object> modelDefinition = (Map<String, Object>) state.value("model_definition").get();

			// 使用PromptService构建求解提示
			String solvingPrompt = promptService.getModelSolvingPrompt(problemAnalysis, modelDefinition);

			// 调用AI进行求解
			Prompt prompt = new Prompt(List.of(new UserMessage(solvingPrompt)));
			String response = chatClient.prompt(prompt).call().content();

			// 解析求解结果
			Map<String, Object> solutionResult = parseSolutionResult(response);

			log.info("模型求解完成: {}", solutionResult.get("summary"));

			return Map.of("solution_result", solutionResult);

		}
		catch (Exception e) {
			log.error("求解器节点执行失败", e);
			return Map.of("error", "模型求解失败: " + e.getMessage());
		}
	}


	private Map<String, Object> parseSolutionResult(String response) {
		try {
			return objectMapper.readValue(response, Map.class);
		}
		catch (Exception e) {
			log.warn("无法解析AI响应为JSON，使用文本格式", e);
			return Map.of("raw_response", response, "summary", "模型求解完成，但格式解析失败");
		}
	}

}
