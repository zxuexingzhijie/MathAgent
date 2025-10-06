package com.mathmodel.core.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoderToWriter;
import com.mathmodel.schema.enums.CompTemplate;
import com.mathmodel.schema.enums.FormatOutput;
import com.mathmodel.service.ScholarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

/**
 * Writer Agent - Generates academic paper based on modeling and coding results
 */
@Slf4j
public class WriterAgent extends BaseAgent {

    private final PromptLoader promptLoader;
    private final ScholarService scholarService;
    private final CompTemplate compTemplate;
    private final FormatOutput formatOutput;

    public WriterAgent(
            String taskId,
            DashScopeChatModel chatModel,
            int maxChatTurns,
            PromptLoader promptLoader,
            ScholarService scholarService,
            CompTemplate compTemplate,
            FormatOutput formatOutput) {
        super(taskId, chatModel, maxChatTurns);
        this.promptLoader = promptLoader;
        this.scholarService = scholarService;
        this.compTemplate = compTemplate;
        this.formatOutput = formatOutput;
    }

    @Override
    public String run(Object input) throws Exception {
        if (!(input instanceof CoderToWriter coderOutput)) {
            throw new IllegalArgumentException("Input must be CoderToWriter");
        }

        log.info("[WriterAgent] Starting paper generation for task: {}", taskId);

        // Add system prompt
        String systemPrompt = buildSystemPrompt();
        appendSystemMessage(systemPrompt);

        // Add user message with results
        String userMessage = buildUserMessage(coderOutput);
        appendUserMessage(userMessage);

        // Generate paper
        ChatResponse response = chat();
        String paper = response.getResult().getOutput().getText();

        if (paper == null || paper.isEmpty()) {
            throw new IllegalArgumentException("Generated paper is empty");
        }

        log.info("[WriterAgent] Paper generated successfully ({} characters)", paper.length());

        // Search for relevant literature (optional enhancement)
        try {
            List<ScholarService.Paper> papers = scholarService.searchPapers(
                    extractKeywords(paper), 
                    3
            );
            
            if (!papers.isEmpty()) {
                String citations = scholarService.formatCitations(papers);
                paper += "\n\n" + citations;
                log.info("[WriterAgent] Added {} references", papers.size());
            }
        } catch (Exception e) {
            log.warn("[WriterAgent] Failed to fetch references, continuing without them", e);
        }

        // Format according to output format
        paper = formatPaper(paper);

        return paper;
    }

    /**
     * Build system prompt with template and format requirements
     */
    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append(promptLoader.getWriterPrompt());
        sb.append("\n\n");

        // Add template-specific requirements
        sb.append("竞赛模板: ").append(getTemplateDescription()).append("\n");
        sb.append("输出格式: ").append(formatOutput).append("\n\n");

        // Add format-specific instructions
        if (formatOutput == FormatOutput.LATEX) {
            sb.append("请使用 LaTeX 格式编写，包括:\n");
            sb.append("- \\documentclass{article}\n");
            sb.append("- \\usepackage{amsmath, graphicx}\n");
            sb.append("- \\begin{document} ... \\end{document}\n");
        } else {
            sb.append("请使用 Markdown 格式编写，包括:\n");
            sb.append("- 清晰的标题层级 (# ## ###)\n");
            sb.append("- 代码块使用 ```python\n");
            sb.append("- 图片引用使用 ![](path)\n");
        }

        return sb.toString();
    }

    /**
     * Build user message with modeling and coding results
     */
    private String buildUserMessage(CoderToWriter coderOutput) {
        StringBuilder sb = new StringBuilder();
        sb.append("请根据以下建模和编程结果撰写完整的学术论文:\n\n");

        // Code and execution results
        sb.append("## 代码实现\n\n");
        sb.append("```python\n");
        sb.append(coderOutput.getCodeResponse());
        sb.append("\n```\n\n");

        // Execution output
        if (coderOutput.getExecutionOutput() != null && !coderOutput.getExecutionOutput().isEmpty()) {
            sb.append("## 执行结果\n\n");
            sb.append("```\n");
            sb.append(coderOutput.getExecutionOutput());
            sb.append("\n```\n\n");
        }

        // Images
        if (coderOutput.getCreatedImages() != null && !coderOutput.getCreatedImages().isEmpty()) {
            sb.append("## 生成的图表\n\n");
            for (int i = 0; i < coderOutput.getCreatedImages().size(); i++) {
                String imagePath = coderOutput.getCreatedImages().get(i);
                sb.append(String.format("图 %d: %s\n", i + 1, imagePath));
            }
            sb.append("\n");
        }

        sb.append("\n请撰写包含以下部分的完整论文:\n");
        sb.append("1. 摘要\n");
        sb.append("2. 问题重述\n");
        sb.append("3. 模型假设\n");
        sb.append("4. 符号说明\n");
        sb.append("5. 模型建立\n");
        sb.append("6. 模型求解\n");
        sb.append("7. 结果分析与讨论\n");
        sb.append("8. 模型评价与改进\n");
        sb.append("9. 结论\n");

        return sb.toString();
    }

    /**
     * Get template description
     */
    private String getTemplateDescription() {
        return switch (compTemplate) {
            case CHINA -> "中国数学建模竞赛（国赛）";
            case USA -> "美国大学生数学建模竞赛（MCM/ICM）";
            case CUSTOM -> "自定义模板";
        };
    }

    /**
     * Format paper according to output format
     */
    private String formatPaper(String paper) {
        if (formatOutput == FormatOutput.LATEX) {
            // Ensure LaTeX document structure
            if (!paper.contains("\\documentclass")) {
                paper = "\\documentclass{article}\n" +
                        "\\usepackage{amsmath}\n" +
                        "\\usepackage{graphicx}\n" +
                        "\\usepackage[UTF8]{ctex}\n" +
                        "\\begin{document}\n\n" +
                        paper +
                        "\n\n\\end{document}";
            }
        }
        return paper;
    }

    /**
     * Extract keywords from paper for literature search
     */
    private String extractKeywords(String paper) {
        // Simple keyword extraction: take first 100 characters as context
        String preview = paper.length() > 100 ? paper.substring(0, 100) : paper;
        
        // Remove special characters and return
        return preview.replaceAll("[^a-zA-Z0-9\\s\\u4e00-\\u9fa5]", " ")
                     .replaceAll("\\s+", " ")
                     .trim();
    }
}
