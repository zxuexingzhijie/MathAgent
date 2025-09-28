package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.chat.ChatClient;
import com.alibaba.cloud.ai.chat.messages.AssistantMessage;
import com.alibaba.cloud.ai.chat.messages.UserMessage;
import com.alibaba.cloud.ai.chat.prompt.Prompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.alibaba.cloud.ai.constant.Constant.*;

/**
 * 报告生成节点
 * 生成完整的研究报告和可视化
 */
@Slf4j
@Component
public class ReportGeneratorNode implements NodeAction {

    @Autowired
    private ChatClient chatClient;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("开始执行报告生成节点...");
        
        try {
            Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
            Map<String, Object> collectedData = (Map<String, Object>) state.value("collected_data").get();
            Map<String, Object> modelDefinition = (Map<String, Object>) state.value("model_definition").get();
            Map<String, Object> solutionResult = (Map<String, Object>) state.value("solution_result").get();
            
            // 构建报告生成提示
            String reportPrompt = buildReportPrompt(problemAnalysis, collectedData, modelDefinition, solutionResult);
            
            // 调用AI生成报告
            Prompt prompt = new Prompt(List.of(new UserMessage(reportPrompt)));
            AssistantMessage response = chatClient.prompt(prompt).call().content();
            
            // 生成最终报告
            String finalReport = generateFinalReport(response.getContent(), problemAnalysis, collectedData, modelDefinition, solutionResult);
            
            // 更新状态
            state.putValue(RESULT, finalReport);
            
            log.info("报告生成完成");
            
        } catch (Exception e) {
            log.error("报告生成节点执行失败", e);
            state.putValue("error", "报告生成失败: " + e.getMessage());
            state.putValue(RESULT, "报告生成失败，请检查错误信息");
        }
        
        return state;
    }

    private String buildReportPrompt(Map<String, Object> problemAnalysis, 
                                   Map<String, Object> collectedData,
                                   Map<String, Object> modelDefinition,
                                   Map<String, Object> solutionResult) {
        return String.format("""
            基于以下完整的数学建模过程，生成专业的研究报告：
            
            问题分析结果：
            %s
            
            数据收集结果：
            %s
            
            模型定义：
            %s
            
            求解结果：
            %s
            
            请生成包含以下部分的完整报告：
            1. 摘要（Executive Summary）
            2. 问题描述和分析
            3. 数据收集和处理
            4. 模型构建
            5. 求解过程和结果
            6. 结果分析和解释
            7. 敏感性分析
            8. 结论和建议
            9. 参考文献
            10. 附录（代码、数据等）
            
            报告要求：
            - 结构清晰，逻辑严密
            - 专业术语使用准确
            - 包含必要的图表说明
            - 提供可操作的建议
            - 符合学术论文格式
            """, problemAnalysis, collectedData, modelDefinition, solutionResult);
    }

    private String generateFinalReport(String aiReport, 
                                     Map<String, Object> problemAnalysis,
                                     Map<String, Object> collectedData,
                                     Map<String, Object> modelDefinition,
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
