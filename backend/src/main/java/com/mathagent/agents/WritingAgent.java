package com.mathagent.agents;

import com.mathagent.exception.PromptProcessingException;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.mathagent.exception.PromptProcessingException;
import com.mathagent.util.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 论文手Agent 专门负责学术论文撰写和格式化
 * 
 * @author Makoto
 */
@Slf4j
@Component
public class WritingAgent implements NodeAction {

	@Autowired
	@Qualifier("writingChatClient")
	private ChatClient writingChatClient;

	@Autowired
	private PromptService promptService;


	@Override
	public Map<String, Object> apply(OverAllState state) {
		log.info("论文手开始工作...");

		try {
			Map<String, Object> modelingResult = (Map<String, Object>) state.value("modeling_analysis").get();
			Map<String, Object> codingResult = (Map<String, Object>) state.value("coding_result").get();
			String problemStatement = state.value("input").get().toString();

			// 生成论文
			String paper = generatePaper(problemStatement, modelingResult, codingResult);

			// 格式化论文
			String formattedPaper = formatPaper(paper);

			// 生成论文摘要
			String abstractContent = generateAbstract(problemStatement, modelingResult, codingResult);

			// 生成关键词
			String keywords = generateKeywords(modelingResult);

			Map<String, Object> writingResult = Map.of("paper", formattedPaper, "abstract", abstractContent, "keywords",
					keywords, "word_count", formattedPaper.length(), "summary", "论文生成完成");

			log.info("论文手完成工作: {}", writingResult.get("summary"));

			return Map.of("generated_paper", paper, "formatted_paper", formattedPaper, "paper_abstract",
					abstractContent, "paper_keywords", keywords, "writing_result", writingResult);

		}
		catch (PromptProcessingException e) {
			return ExceptionHandler.handlePromptException("论文生成", e);
		}
		catch (Exception e) {
			return ExceptionHandler.handleAgentException("论文手", e);
		}
	}

	private String generatePaper(String problemStatement, Map<String, Object> modelingResult,
			Map<String, Object> codingResult) throws PromptProcessingException {
		// 使用提示词服务构建论文生成提示
		String writingPrompt = promptService.getPaperGenerationPrompt(problemStatement, modelingResult, codingResult);

		Prompt prompt = new Prompt(List.of(new UserMessage(writingPrompt)));
		String response = writingChatClient.prompt(prompt).call().content();

		return response;
	}

	private String formatPaper(String paper) throws PromptProcessingException {
		// 使用提示词服务构建论文格式化提示
		String formatPrompt = promptService.getPaperFormattingPrompt(paper);

		Prompt prompt = new Prompt(List.of(new UserMessage(formatPrompt)));
		String response = writingChatClient.prompt(prompt).call().content();

		return response;
	}

	private String generateAbstract(String problemStatement, Map<String, Object> modelingResult,
			Map<String, Object> codingResult) throws PromptProcessingException {
		// 使用提示词服务构建摘要生成提示
		String abstractPrompt = promptService.getAbstractGenerationPrompt(problemStatement, modelingResult,
				codingResult);

		Prompt prompt = new Prompt(List.of(new UserMessage(abstractPrompt)));
		String response = writingChatClient.prompt(prompt).call().content();

		return response;
	}

	private String generateKeywords(Map<String, Object> modelingResult) throws PromptProcessingException {
		// 使用提示词服务构建关键词生成提示
		String keywordsPrompt = promptService.getKeywordsGenerationPrompt(modelingResult);

		Prompt prompt = new Prompt(List.of(new UserMessage(keywordsPrompt)));
		String response = writingChatClient.prompt(prompt).call().content();

		return response;
	}

}