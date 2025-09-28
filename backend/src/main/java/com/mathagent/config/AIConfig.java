package com.mathagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI Alibaba配置 配置DashScope模型
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
	public DashScopeApi dashScopeApi() {
		return DashScopeApi.builder().apiKey(apiKey).build();
	}

	@Bean
	public DashScopeChatModel dashScopeChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder().withModel(chatModel).build())
			.build();
	}

	@Bean
	public DashScopeEmbeddingModel dashScopeEmbeddingModel(DashScopeApi dashScopeApi) {
		return new DashScopeEmbeddingModel(dashScopeApi);
	}

}
