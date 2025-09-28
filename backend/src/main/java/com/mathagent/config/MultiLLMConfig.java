package com.mathagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Multi-LLMs配置 为不同的Agent配置不同的模型
 */
@Configuration
public class MultiLLMConfig {

	@Value("${spring.ai.dashscope.api-key}")
	private String apiKey;

	/**
	 * 建模手专用模型 - 擅长数学建模和问题分析
	 */
	@Bean("modelingChatModel")
	public DashScopeChatModel modelingChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder()
				.withModel("qwen-max") // 使用最强模型进行建模
				.withTemperature(0.3) // 低温度确保准确性
                .withMaxToken(4000)
				.build())
			.build();
	}

	/**
	 * 代码手专用模型 - 擅长代码生成和调试
	 */
	@Bean("codingChatModel")
	public DashScopeChatModel codingChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder()
				.withModel("qwen-plus") // 使用平衡模型
				.withTemperature(0.5) // 中等温度平衡创造性和准确性
				.withMaxToken(6000) // 更多token用于代码
				.build())
			.build();
	}

	/**
	 * 论文手专用模型 - 擅长学术写作和论文生成
	 */
	@Bean("writingChatModel")
	public DashScopeChatModel writingChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder()
				.withModel("qwen-max") // 使用最强模型进行写作
				.withTemperature(0.7) // 较高温度增加创造性
				.withMaxToken(8000) // 最多token用于长文本
				.build())
			.build();
	}

	/**
	 * 通用分析模型 - 用于数据分析和结果解释
	 */
	@Bean("analysisChatModel")
	public DashScopeChatModel analysisChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder()
				.withModel("qwen-turbo") // 使用快速模型
				.withTemperature(0.4) // 较低温度确保分析准确性
				.withMaxToken(3000)
				.build())
			.build();
	}

	/**
	 * 建模手专用ChatClient
	 */
	@Bean("modelingChatClient")
	public ChatClient modelingChatClient(@Qualifier("modelingChatModel") DashScopeChatModel modelingChatModel) {
		return ChatClient.builder(modelingChatModel).build();
	}

	/**
	 * 代码手专用ChatClient
	 */
	@Bean("codingChatClient")
	public ChatClient codingChatClient(@Qualifier("codingChatModel") DashScopeChatModel codingChatModel) {
		return ChatClient.builder(codingChatModel).build();
	}

	/**
	 * 论文手专用ChatClient
	 */
	@Bean("writingChatClient")
	public ChatClient writingChatClient(@Qualifier("writingChatModel") DashScopeChatModel writingChatModel) {
		return ChatClient.builder(writingChatModel).build();
	}

	/**
	 * 通用分析ChatClient
	 */
	@Bean("analysisChatClient")
	public ChatClient analysisChatClient(@Qualifier("analysisChatModel") DashScopeChatModel analysisChatModel) {
		return ChatClient.builder(analysisChatModel).build();
	}

}
