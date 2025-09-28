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
 * 数据收集节点 根据问题分析结果收集相关数据和文献
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class DataCollectorNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PromptService promptService;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("开始执行数据收集节点...");

		try {
			Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();

			// 使用PromptService构建数据收集提示
			String collectionPrompt = promptService.getDataCollectionPrompt(problemAnalysis);

			// 调用AI进行数据收集规划
			Prompt prompt = new Prompt(List.of(new UserMessage(collectionPrompt)));
			String response = chatClient.prompt(prompt).call().content();

			// 解析收集结果
			Map<String, Object> collectionResult = parseCollectionResult(response);

			log.info("数据收集完成: {}", collectionResult.get("summary"));

			return Map.of("collected_data", collectionResult);

		}
		catch (Exception e) {
			log.error("数据收集节点执行失败", e);
			return Map.of("error", "数据收集失败: " + e.getMessage());
		}
	}

	private Map<String, Object> parseCollectionResult(String response) {
		try {
			return objectMapper.readValue(response, Map.class);
		}
		catch (Exception e) {
			log.warn("无法解析AI响应为JSON，使用文本格式", e);
			return Map.of("raw_response", response, "summary", "数据收集计划完成，但格式解析失败");
		}
	}

}
