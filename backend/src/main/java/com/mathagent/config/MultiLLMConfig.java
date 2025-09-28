package com.mathagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Multi-LLMs配置 为不同的Agent配置不同的模型
 * 
 * @author Makoto
 */
@Configuration
public class MultiLLMConfig {

	@Value("${spring.ai.dashscope.api-key}")
	private String apiKey;

	// 模型配置常量 - 默认使用百炼模型
	private static final String MODELING_MODEL = "qwen-max";
	private static final String CODING_MODEL = "qwen-max";
	private static final String WRITING_MODEL = "qwen-max";
	private static final String ANALYSIS_MODEL = "qwen-max";
	
	// 温度参数常量
	private static final double MODELING_TEMPERATURE = 0.3;
	private static final double CODING_TEMPERATURE = 0.5;
	private static final double WRITING_TEMPERATURE = 0.7;
	private static final double ANALYSIS_TEMPERATURE = 0.4;
	
	// Token限制常量
	private static final int MODELING_MAX_TOKENS = 4000;
	private static final int CODING_MAX_TOKENS = 6000;
	private static final int WRITING_MAX_TOKENS = 8000;
	private static final int ANALYSIS_MAX_TOKENS = 3000;

	/**
	 * 建模手专用模型 - 擅长数学建模和问题分析
	 */
	@Bean("modelingChatModel")
	public DashScopeChatModel modelingChatModel(DashScopeApi dashScopeApi) {
		return DashScopeChatModel.builder()
			.dashScopeApi(dashScopeApi)
			.defaultOptions(DashScopeChatOptions.builder()
				.withModel(MODELING_MODEL)
				.withTemperature(MODELING_TEMPERATURE)
				.withMaxToken(MODELING_MAX_TOKENS)
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
				.withModel(CODING_MODEL)
				.withTemperature(CODING_TEMPERATURE)
				.withMaxToken(CODING_MAX_TOKENS)
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
				.withModel(WRITING_MODEL)
				.withTemperature(WRITING_TEMPERATURE)
				.withMaxToken(WRITING_MAX_TOKENS)
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
				.withModel(ANALYSIS_MODEL)
				.withTemperature(ANALYSIS_TEMPERATURE)
				.withMaxToken(ANALYSIS_MAX_TOKENS)
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
