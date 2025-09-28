package com.mathagent.config;

import com.alibaba.cloud.ai.dashscope.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.DashScopeEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba配置
 * 配置DashScope模型
 */
@Configuration
public class AIConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.model}")
    private String chatModel;

    @Value("${spring.ai.dashscope.embedding.model}")
    private String embeddingModel;

    @Bean
    public DashScopeChatModel dashScopeChatModel() {
        return DashScopeChatModel.builder()
                .apiKey(apiKey)
                .model(chatModel)
                .build();
    }

    @Bean
    public DashScopeEmbeddingModel dashScopeEmbeddingModel() {
        return DashScopeEmbeddingModel.builder()
                .apiKey(apiKey)
                .model(embeddingModel)
                .build();
    }
}
