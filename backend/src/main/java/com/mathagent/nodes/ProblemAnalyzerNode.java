package com.mathagent.nodes;

import com.alibaba.cloud.ai.graph.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.chat.ChatClient;
import com.alibaba.cloud.ai.chat.messages.AssistantMessage;
import com.alibaba.cloud.ai.chat.messages.UserMessage;
import com.alibaba.cloud.ai.chat.prompt.Prompt;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.alibaba.cloud.ai.constant.Constant.*;

/**
 * 问题分析节点
 * 分析数学建模问题，提取关键信息和要求
 */
@Slf4j
@Component
public class ProblemAnalyzerNode implements NodeAction {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("开始执行问题分析节点...");
        
        try {
            String problemStatement = state.value(INPUT_KEY).get().toString();
            
            // 构建分析提示
            String analysisPrompt = buildAnalysisPrompt(problemStatement);
            
            // 调用AI进行分析
            Prompt prompt = new Prompt(List.of(new UserMessage(analysisPrompt)));
            AssistantMessage response = chatClient.prompt(prompt).call().content();
            
            // 解析分析结果
            Map<String, Object> analysisResult = parseAnalysisResult(response.getContent());
            
            // 更新状态
            state.putValue("problem_analysis", analysisResult);
            
            log.info("问题分析完成: {}", analysisResult.get("summary"));
            
        } catch (Exception e) {
            log.error("问题分析节点执行失败", e);
            state.putValue("error", "问题分析失败: " + e.getMessage());
        }
        
        return state;
    }

    private String buildAnalysisPrompt(String problemStatement) {
        return String.format("""
            请分析以下数学建模问题，并提取关键信息：
            
            问题描述：
            %s
            
            请从以下维度进行分析：
            1. 问题类型（优化、预测、分类、仿真等）
            2. 核心目标和要求
            3. 已知条件和约束
            4. 需要求解的未知量
            5. 数据需求
            6. 模型复杂度评估
            7. 可能的建模方法
            8. 预期输出格式
            
            请以JSON格式返回分析结果，包含以下字段：
            - problem_type: 问题类型
            - objectives: 核心目标列表
            - constraints: 约束条件列表
            - unknowns: 待求解变量列表
            - data_requirements: 数据需求
            - complexity_level: 复杂度等级（低/中/高）
            - suggested_methods: 建议的建模方法
            - expected_outputs: 预期输出
            - summary: 问题摘要
            """, problemStatement);
    }

    private Map<String, Object> parseAnalysisResult(String response) {
        try {
            // 尝试解析JSON响应
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("无法解析AI响应为JSON，使用文本格式", e);
            // 如果无法解析为JSON，返回文本格式
            return Map.of(
                "raw_response", response,
                "summary", "问题分析完成，但格式解析失败"
            );
        }
    }
}
