package com.mathagent.agents;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.service.CodeInterpreterService;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 代码手Agent 专门负责代码生成、执行和调试
 */
@Slf4j
@Component
public class CodingAgent implements NodeAction {

	@Autowired
	@Qualifier("codingChatModel")
	private ChatClient codingChatClient;

	@Autowired
	private CodeInterpreterService codeInterpreterService;

	@Autowired
	private PromptService promptService;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("代码手开始工作...");

		try {
			Map<String, Object> modelingResult = (Map<String, Object>) state.value("modeling_analysis").get();
			String taskId = state.value("task_id").get().toString();

			// 创建代码执行会话
			CodeInterpreterService.JupyterSession session = codeInterpreterService.createSession(taskId);
			state.value("code_session_id", session.getSessionId());

			// 生成代码
			String code = generateCode(modelingResult);
			state.value("generated_code", code);

			// 执行代码
			CodeInterpreterService.CodeExecutionResult result = codeInterpreterService
				.executeCode(session.getSessionId(), code, "python");

			// 处理执行结果
			Map<String, Object> codingResult = processExecutionResult(result, code);
			state.value("coding_result", codingResult);

			// 如果执行失败，尝试调试
			if (!result.isSuccess()) {
				String debugCode = debugCode(code, result.getError());
				CodeInterpreterService.CodeExecutionResult debugResult = codeInterpreterService
					.executeCode(session.getSessionId(), debugCode, "python");
				state.value("debug_result", processExecutionResult(debugResult, debugCode));
			}

			log.info("代码手完成工作: {}", codingResult.get("summary"));

		}
		catch (Exception e) {
			log.error("代码手工作失败", e);
			state.value("error", "代码生成执行失败: " + e.getMessage());
		}

		return state.data();
	}

	private String generateCode(Map<String, Object> modelingResult) {
		// 使用提示词服务构建代码生成提示
		String codingPrompt = promptService.getCodeGenerationPrompt(modelingResult);

		Prompt prompt = new Prompt(List.of(new UserMessage(codingPrompt)));
		AssistantMessage response = codingChatClient.prompt(prompt).call().content();

		return response.getContent();
	}

	private String debugCode(String originalCode, String error) {
		// 使用提示词服务构建代码调试提示
		String debugPrompt = promptService.getCodeDebugPrompt(originalCode, error);

		Prompt prompt = new Prompt(List.of(new UserMessage(debugPrompt)));
		AssistantMessage response = codingChatClient.prompt(prompt).call().content();

		return response.getContent();
	}

	private Map<String, Object> processExecutionResult(CodeInterpreterService.CodeExecutionResult result, String code) {
		Map<String, Object> codingResult = Map.of("code", code, "success", result.isSuccess(), "output",
				result.getOutput(), "error", result.getError(), "execution_time",
				result.getEndTime() != null && result.getStartTime() != null
						? java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis() : 0,
				"summary", result.isSuccess() ? "代码执行成功" : "代码执行失败");

		return codingResult;
	}

}
