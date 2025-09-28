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
 * 数据收集节点
 * 根据问题分析结果收集相关数据和文献
 */
@Slf4j
@Component
public class DataCollectorNode implements NodeAction {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("开始执行数据收集节点...");
        
        try {
            Map<String, Object> problemAnalysis = (Map<String, Object>) state.value("problem_analysis").get();
            
            // 构建数据收集提示
            String collectionPrompt = buildCollectionPrompt(problemAnalysis);
            
            // 调用AI进行数据收集规划
            Prompt prompt = new Prompt(List.of(new UserMessage(collectionPrompt)));
            AssistantMessage response = chatClient.prompt(prompt).call().content();
            
            // 解析收集结果
            Map<String, Object> collectionResult = parseCollectionResult(response.getContent());
            
            // 更新状态
            state.putValue("collected_data", collectionResult);
            
            log.info("数据收集完成: {}", collectionResult.get("summary"));
            
        } catch (Exception e) {
            log.error("数据收集节点执行失败", e);
            state.putValue("error", "数据收集失败: " + e.getMessage());
        }
        
        return state;
    }

    private String buildCollectionPrompt(Map<String, Object> problemAnalysis) {
        return String.format("""
            基于以下问题分析结果，制定数据收集计划：
            
            问题分析结果：
            %s
            
            请制定详细的数据收集计划，包括：
            1. 需要收集的数据类型
            2. 数据来源建议
            3. 数据收集方法
            4. 数据预处理需求
            5. 数据质量要求
            6. 数据存储格式
            7. 数据验证方法
            8. 收集优先级和时间安排
            
            请以JSON格式返回收集计划，包含以下字段：
            - data_types: 数据类型列表
            - data_sources: 数据来源建议
            - collection_methods: 收集方法
            - preprocessing_needs: 预处理需求
            - quality_requirements: 质量要求
            - storage_format: 存储格式
            - validation_methods: 验证方法
            - priority_order: 优先级排序
            - estimated_time: 预估时间
            - summary: 收集计划摘要
            """, problemAnalysis);
    }

    private Map<String, Object> parseCollectionResult(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("无法解析AI响应为JSON，使用文本格式", e);
            return Map.of(
                "raw_response", response,
                "summary", "数据收集计划完成，但格式解析失败"
            );
        }
    }
}
