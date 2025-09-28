package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 模型构建节点 基于收集的数据构建数学模型
 */
@Slf4j
@Component
public class ModelBuilderNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public OverAllState apply(OverAllState state) {
		log.info("开始执行模型构建节点...");

		try {
			Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
			Map<String, Object> collectedData = (Map<String, Object>) state.value("collected_data").get();

			// 构建模型构建提示
			String modelPrompt = buildModelPrompt(problemAnalysis, collectedData);

			// 调用AI进行模型构建
			Prompt prompt = new Prompt(List.of(new UserMessage(modelPrompt)));
			AssistantMessage response = chatClient.prompt(prompt).call().content();

			// 解析模型定义
			Map<String, Object> modelDefinition = parseModelDefinition(response.getContent());

			// 更新状态
			state.put("model_definition", modelDefinition);

			log.info("模型构建完成: {}", modelDefinition.get("model_name"));

		}
		catch (Exception e) {
			log.error("模型构建节点执行失败", e);
			state.put("error", "模型构建失败: " + e.getMessage());
		}

		return state;
	}

	private String buildModelPrompt(Map<String, Object> problemAnalysis, Map<String, Object> collectedData) {
		return String.format("""
				基于以下问题分析和数据收集结果，构建数学模型：

				问题分析结果：
				%s

				数据收集结果：
				%s

				请构建合适的数学模型，包括：
				1. 模型类型选择（线性/非线性、确定性/随机性等）
				2. 变量定义和关系
				3. 目标函数或评价指标
				4. 约束条件
				5. 模型参数
				6. 求解算法建议
				7. 模型验证方法
				8. 敏感性分析计划

				请以JSON格式返回模型定义，包含以下字段：
				- model_name: 模型名称
				- model_type: 模型类型
				- variables: 变量定义
				- objective_function: 目标函数
				- constraints: 约束条件
				- parameters: 模型参数
				- solving_algorithm: 求解算法
				- validation_method: 验证方法
				- sensitivity_analysis: 敏感性分析
				- model_code: 模型代码（如适用）
				- summary: 模型摘要
				""", problemAnalysis, collectedData);
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
