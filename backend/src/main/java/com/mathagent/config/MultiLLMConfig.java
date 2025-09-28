package com.mathagent.config;

import com.alibaba.cloud.ai.dashscope.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.DashScopeMultimodalModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Multi-LLMs配置
 * 为不同的Agent配置不同的模型
 */
@Configuration
public class MultiLLMConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    /**
     * 建模手专用模型 - 擅长数学建模和问题分析
     */
    @Bean("modelingChatModel")
    public DashScopeChatModel modelingChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .model("qwen-max")  // 使用最强模型进行建模
                .temperature(0.3)   // 低温度确保准确性
                .maxTokens(4000)
                .build();
    }

    /**
     * 代码手专用模型 - 擅长代码生成和调试
     */
    @Bean("codingChatModel")
    public DashScopeChatModel codingChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .model("qwen-plus")  // 使用平衡模型
                .temperature(0.5)    // 中等温度平衡创造性和准确性
                .maxTokens(6000)      // 更多token用于代码
                .build();
    }

    /**
     * 论文手专用模型 - 擅长学术写作和论文生成
     */
    @Bean("writingChatModel")
    public DashScopeChatModel writingChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .model("qwen-max")   // 使用最强模型进行写作
                .temperature(0.7)    // 较高温度增加创造性
                .maxTokens(8000)     // 最多token用于长文本
                .build();
    }

    /**
     * 通用分析模型 - 用于数据分析和结果解释
     */
    @Bean("analysisChatModel")
    public DashScopeChatModel analysisChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .model("qwen-turbo")  // 使用快速模型
                .temperature(0.4)     // 较低温度确保分析准确性
                .maxTokens(3000)
                .build();
    }

    /**
     * 嵌入模型
     */
    @Bean
    public DashScopeEmbeddingModel dashScopeEmbeddingModel() {
        return DashScopeEmbeddingModel.builder()
                .apiKey(apiKey)
                .model("text-embedding-v3")
                .build();
    }

    /**
     * 多模态模型 - 用于图像和文档分析
     */
    @Bean
    public DashScopeMultimodalModel dashScopeMultimodalModel() {
        return DashScopeMultimodalModel.builder()
                .apiKey(apiKey)
                .model("qwen-vl-max")
                .build();
    }
}
