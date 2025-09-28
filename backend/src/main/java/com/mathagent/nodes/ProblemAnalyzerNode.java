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

import static com.alibaba.cloud.ai.graph.OverAllState.DEFAULT_INPUT_KEY;

/**
 * 问题分析节点 分析数学建模问题，提取关键信息和要求
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class ProblemAnalyzerNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PromptService promptService;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("开始执行问题分析节点...");

		try {
			String problemStatement = state.value(DEFAULT_INPUT_KEY).get().toString();

			// 使用ModelingAgent的问题分析提示词
			String analysisPrompt = promptService.getModelingAnalysisPrompt(problemStatement);

			// 调用AI进行分析
			Prompt prompt = new Prompt(List.of(new UserMessage(analysisPrompt)));
			String response = chatClient.prompt(prompt).call().content();

			// 解析分析结果
			Map<String, Object> analysisResult = parseAnalysisResult(response);

			log.info("问题分析完成: {}", analysisResult.get("summary"));

			return Map.of("problem_analysis", analysisResult);

		}
		catch (Exception e) {
			log.error("问题分析节点执行失败", e);
			return Map.of("error", "问题分析失败: " + e.getMessage());
		}
	}

	private Map<String, Object> parseAnalysisResult(String response) {
		try {
			// 尝试解析JSON响应
			return objectMapper.readValue(response, Map.class);
		}
		catch (Exception e) {
			log.warn("无法解析AI响应为JSON，使用文本格式", e);
			// 如果无法解析为JSON，返回文本格式
			return Map.of("raw_response", response, "summary", "问题分析完成，但格式解析失败");
		}
	}

}
