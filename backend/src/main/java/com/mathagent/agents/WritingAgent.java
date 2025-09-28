package com.mathagent.agents;

import com.alibaba.cloud.ai.chat.ChatClient;
import com.alibaba.cloud.ai.chat.messages.AssistantMessage;
import com.alibaba.cloud.ai.chat.messages.UserMessage;
import com.alibaba.cloud.ai.chat.prompt.Prompt;
import com.alibaba.cloud.ai.graph.NodeAction;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.service.PromptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 论文手Agent
 * 专门负责学术论文撰写和格式化
 */
@Slf4j
@Component
public class WritingAgent implements NodeAction {

    @Autowired
    @Qualifier("writingChatModel")
    private ChatClient writingChatClient;

    @Autowired
    private PromptService promptService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public OverAllState apply(OverAllState state) {
        log.info("论文手开始工作...");
        
        try {
            Map<String, Object> modelingResult = (Map<String, Object>) state.value("modeling_analysis").get();
            Map<String, Object> codingResult = (Map<String, Object>) state.value("coding_result").get();
            String problemStatement = state.value("input").get().toString();
            
            // 生成论文
            String paper = generatePaper(problemStatement, modelingResult, codingResult);
            state.putValue("generated_paper", paper);
            
            // 格式化论文
            String formattedPaper = formatPaper(paper);
            state.putValue("formatted_paper", formattedPaper);
            
            // 生成论文摘要
            String abstract = generateAbstract(problemStatement, modelingResult, codingResult);
            state.putValue("paper_abstract", abstract);
            
            // 生成关键词
            String keywords = generateKeywords(modelingResult);
            state.putValue("paper_keywords", keywords);
            
            Map<String, Object> writingResult = Map.of(
                "paper", formattedPaper,
                "abstract", abstract,
                "keywords", keywords,
                "word_count", formattedPaper.length(),
                "summary", "论文生成完成"
            );
            
            state.putValue("writing_result", writingResult);
            
            log.info("论文手完成工作: {}", writingResult.get("summary"));
            
        } catch (Exception e) {
            log.error("论文手工作失败", e);
            state.putValue("error", "论文生成失败: " + e.getMessage());
        }
        
        return state;
    }

    private String generatePaper(String problemStatement, Map<String, Object> modelingResult, Map<String, Object> codingResult) {
        // 使用提示词服务构建论文生成提示
        String writingPrompt = promptService.getPaperGenerationPrompt(problemStatement, modelingResult, codingResult);

        Prompt prompt = new Prompt(List.of(new UserMessage(writingPrompt)));
        AssistantMessage response = writingChatClient.prompt(prompt).call().content();
        
        return response.getContent();
    }

    private String formatPaper(String paper) {
        // 使用提示词服务构建论文格式化提示
        String formatPrompt = promptService.getPaperFormattingPrompt(paper);

        Prompt prompt = new Prompt(List.of(new UserMessage(formatPrompt)));
        AssistantMessage response = writingChatClient.prompt(prompt).call().content();
        
        return response.getContent();
    }

    private String generateAbstract(String problemStatement, Map<String, Object> modelingResult, Map<String, Object> codingResult) {
        // 使用提示词服务构建摘要生成提示
        String abstractPrompt = promptService.getAbstractGenerationPrompt(problemStatement, modelingResult, codingResult);

        Prompt prompt = new Prompt(List.of(new UserMessage(abstractPrompt)));
        AssistantMessage response = writingChatClient.prompt(prompt).call().content();
        
        return response.getContent();
    }

    private String generateKeywords(Map<String, Object> modelingResult) {
        // 使用提示词服务构建关键词生成提示
        String keywordsPrompt = promptService.getKeywordsGenerationPrompt(modelingResult);

        Prompt prompt = new Prompt(List.of(new UserMessage(keywordsPrompt)));
        AssistantMessage response = writingChatClient.prompt(prompt).call().content();
        
        return response.getContent();
    }
}
