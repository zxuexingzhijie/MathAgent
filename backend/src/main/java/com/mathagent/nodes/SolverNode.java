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

import java.util.List;
import java.util.Map;

/**
 * 求解器节点
 * 执行模型求解和验证
 */
@Slf4j
@Component
public class SolverNode implements NodeAction {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("开始执行求解器节点...");
        
        try {
            Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
            Map<String, Object> modelDefinition = (Map<String, Object>) state.value("model_definition").get();
            
            // 构建求解提示
            String solvingPrompt = buildSolvingPrompt(problemAnalysis, modelDefinition);
            
            // 调用AI进行求解
            Prompt prompt = new Prompt(List.of(new UserMessage(solvingPrompt)));
            AssistantMessage response = chatClient.prompt(prompt).call().content();
            
            // 解析求解结果
            Map<String, Object> solutionResult = parseSolutionResult(response.getContent());
            
            // 更新状态
            state.putValue("solution_result", solutionResult);
            
            log.info("模型求解完成: {}", solutionResult.get("summary"));
            
        } catch (Exception e) {
            log.error("求解器节点执行失败", e);
            state.putValue("error", "模型求解失败: " + e.getMessage());
        }
        
        return state;
    }

    private String buildSolvingPrompt(Map<String, Object> problemAnalysis, Map<String, Object> modelDefinition) {
        return String.format("""
            基于以下问题分析和模型定义，执行模型求解：
            
            问题分析结果：
            %s
            
            模型定义：
            %s
            
            请执行模型求解，包括：
            1. 求解算法实现
            2. 参数设置和优化
            3. 求解过程记录
            4. 结果验证
            5. 敏感性分析
            6. 结果解释
            7. 误差分析
            8. 求解性能评估
            
            请以JSON格式返回求解结果，包含以下字段：
            - algorithm_used: 使用的算法
            - parameters: 求解参数
            - solution_values: 求解结果值
            - objective_value: 目标函数值
            - solving_time: 求解时间
            - convergence_info: 收敛信息
            - validation_results: 验证结果
            - sensitivity_analysis: 敏感性分析结果
            - error_analysis: 误差分析
            - performance_metrics: 性能指标
            - interpretation: 结果解释
            - summary: 求解摘要
            """, problemAnalysis, modelDefinition);
    }

    private Map<String, Object> parseSolutionResult(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("无法解析AI响应为JSON，使用文本格式", e);
            return Map.of(
                "raw_response", response,
                "summary", "模型求解完成，但格式解析失败"
            );
        }
    }
}
