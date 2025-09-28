package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 报告生成节点 生成完整的研究报告和可视化
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class ReportGeneratorNode implements NodeAction {

	@Autowired
	private ChatClient chatClient;

	@Autowired
	private PromptService promptService;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("开始执行报告生成节点...");

		try {
			Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
			Map<String, Object> collectedData = (Map<String, Object>) state.value("collected_data").get();
			Map<String, Object> modelDefinition = (Map<String, Object>) state.value("model_definition").get();
			Map<String, Object> solutionResult = (Map<String, Object>) state.value("solution_result").get();

			// 使用WritingAgent的报告生成提示词
			String reportPrompt = promptService.getPaperGenerationPrompt(
				problemAnalysis.toString(), 
				modelDefinition, 
				solutionResult
			);

			// 调用AI生成报告
			Prompt prompt = new Prompt(List.of(new UserMessage(reportPrompt)));
			String response = chatClient.prompt(prompt).call().content();

			// 生成最终报告
			String finalReport = generateFinalReport(response, problemAnalysis, collectedData,
					modelDefinition, solutionResult);

			log.info("报告生成完成");

			return Map.of("result", finalReport);

		}
		catch (Exception e) {
			log.error("报告生成节点执行失败", e);
			return Map.of("error", "报告生成失败: " + e.getMessage(), "result", "报告生成失败，请检查错误信息");
		}
	}


	private String generateFinalReport(String aiReport, Map<String, Object> problemAnalysis,
			Map<String, Object> collectedData, Map<String, Object> modelDefinition,
			Map<String, Object> solutionResult) {

		StringBuilder report = new StringBuilder();

		// 报告头部
		report.append("# 数学建模研究报告\n\n");
		report.append("**生成时间**: ").append(java.time.LocalDateTime.now()).append("\n\n");

		// AI生成的报告内容
		report.append("## 报告内容\n\n");
		report.append(aiReport).append("\n\n");

		// 详细数据附录
		report.append("## 详细数据附录\n\n");

		report.append("### 问题分析详情\n");
		report.append("```json\n");
		report.append(problemAnalysis.toString()).append("\n```\n\n");

		report.append("### 数据收集详情\n");
		report.append("```json\n");
		report.append(collectedData.toString()).append("\n```\n\n");

		report.append("### 模型定义详情\n");
		report.append("```json\n");
		report.append(modelDefinition.toString()).append("\n```\n\n");

		report.append("### 求解结果详情\n");
		report.append("```json\n");
		report.append(solutionResult.toString()).append("\n```\n\n");

		// 报告尾部
		report.append("---\n");
		report.append("*本报告由Math Modeling DeepResearch Agent自动生成*\n");

		return report.toString();
	}

}
