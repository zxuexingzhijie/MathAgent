package com.mathagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词管理服务 负责加载和管理Agent的提示词模板 支持多文件加载和分类管理
 * 
 * @author Makoto
 */
@Slf4j
@Service
public class PromptService {

	// 提示词文件列表
	private static final String[] PROMPT_FILES = { "prompts/modeling-agent-prompts.md",
			"prompts/coding-agent-prompts.md", "prompts/writing-agent-prompts.md", "prompts/common-prompts.md",
			"prompts/nodes-prompts.md" };

	private Map<String, String> promptTemplates = new HashMap<>();

	private boolean initialized = false;

	/**
	 * 初始化提示词模板
	 */
	public synchronized void initialize() {
		if (initialized) {
			return;
		}

		try {
			// 加载所有提示词文件
			for (String fileName : PROMPT_FILES) {
				loadPromptFile(fileName);
			}

			initialized = true;
			log.info("提示词模板加载完成，共加载 {} 个模板", promptTemplates.size());
		}
		catch (IOException e) {
			log.error("加载提示词文件失败", e);
			throw new RuntimeException("加载提示词文件失败", e);
		}
	}

	/**
	 * 加载单个提示词文件
	 */
	private void loadPromptFile(String fileName) throws IOException {
		ClassPathResource resource = new ClassPathResource(fileName);
		if (!resource.exists()) {
			log.warn("提示词文件不存在: {}", fileName);
			return;
		}

		String content = resource.getContentAsString(StandardCharsets.UTF_8);
		parsePrompts(content, fileName);
		log.debug("加载提示词文件: {}, 解析完成", fileName);
	}

	/**
	 * 解析提示词文件
	 */
	private void parsePrompts(String content, String fileName) {
		// 使用正则表达式匹配提示词块
		Pattern pattern = Pattern.compile("## (.*?)\\n\\n(.*?)(?=\\n## |$)", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(content);

		while (matcher.find()) {
			String promptName = matcher.group(1).trim();
			String promptTemplate = matcher.group(2).trim();

			// 添加文件前缀避免重名
			String fullPromptName = buildPromptName(fileName, promptName);

			promptTemplates.put(fullPromptName, promptTemplate);
			log.debug("加载提示词: {} -> {}", fullPromptName, promptName);
		}
	}

	/**
	 * 构建提示词名称，消除复杂的条件链
	 */
	private String buildPromptName(String fileName, String promptName) {
		if (fileName.contains("modeling")) {
			return "modeling." + promptName;
		}
		if (fileName.contains("coding")) {
			return "coding." + promptName;
		}
		if (fileName.contains("writing")) {
			return "writing." + promptName;
		}
		if (fileName.contains("common")) {
			return "common." + promptName;
		}
		if (fileName.contains("nodes")) {
			return "nodes." + promptName;
		}
		return promptName;
	}

	/**
	 * 获取提示词模板
	 */
	public String getPromptTemplate(String promptName) {
		if (!initialized) {
			initialize();
		}

		String template = promptTemplates.get(promptName);
		if (template == null) {
			log.warn("未找到提示词模板: {}", promptName);
			throw new RuntimeException("未找到提示词模板: " + promptName);
		}

		return template;
	}

	/**
	 * 构建提示词
	 */
	public String buildPrompt(String promptName, Map<String, Object> variables) {
		String template = getPromptTemplate(promptName);
		return replaceVariables(template, variables);
	}

	/**
	 * 替换模板变量
	 */
	private String replaceVariables(String template, Map<String, Object> variables) {
		String result = template;

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
			String placeholder = "{" + entry.getKey() + "}";
			String value = entry.getValue() != null ? entry.getValue().toString() : "";
			result = result.replace(placeholder, value);
		}

		return result;
	}

	// ========== 建模手Agent提示词 ==========

	/**
	 * 获取建模手问题分析提示词
	 */
	public String getModelingAnalysisPrompt(String problemStatement) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemStatement", problemStatement);
		return buildPrompt("modeling.问题分析提示词", variables);
	}

	/**
	 * 获取模型验证提示词
	 */
	public String getModelValidationPrompt(String modelDefinition) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("modelDefinition", modelDefinition);
		return buildPrompt("modeling.模型验证提示词", variables);
	}

	/**
	 * 获取复杂度评估提示词
	 */
	public String getComplexityAssessmentPrompt(String problemStatement) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemStatement", problemStatement);
		return buildPrompt("modeling.复杂度评估提示词", variables);
	}

	// ========== 代码手Agent提示词 ==========

	/**
	 * 获取代码生成提示词
	 */
	public String getCodeGenerationPrompt(Object modelingResult) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("modelingResult", modelingResult);
		return buildPrompt("coding.代码生成提示词", variables);
	}

	/**
	 * 获取代码调试提示词
	 */
	public String getCodeDebugPrompt(String originalCode, String error) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("originalCode", originalCode);
		variables.put("error", error);
		return buildPrompt("coding.代码调试提示词", variables);
	}

	/**
	 * 获取代码优化提示词
	 */
	public String getCodeOptimizationPrompt(String originalCode) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("originalCode", originalCode);
		return buildPrompt("coding.代码优化提示词", variables);
	}

	/**
	 * 获取算法实现提示词
	 */
	public String getAlgorithmImplementationPrompt(String algorithmDescription) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("algorithmDescription", algorithmDescription);
		return buildPrompt("coding.算法实现提示词", variables);
	}

	/**
	 * 获取可视化代码提示词
	 */
	public String getVisualizationCodePrompt(String dataDescription) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("dataDescription", dataDescription);
		return buildPrompt("coding.可视化代码提示词", variables);
	}

	// ========== 论文手Agent提示词 ==========

	/**
	 * 获取论文生成提示词
	 */
	public String getPaperGenerationPrompt(String problemStatement, Object modelingResult, Object codingResult) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemStatement", problemStatement);
		variables.put("modelingResult", modelingResult);
		variables.put("codingResult", codingResult);
		return buildPrompt("writing.论文生成提示词", variables);
	}

	/**
	 * 获取论文格式化提示词
	 */
	public String getPaperFormattingPrompt(String paper) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("paper", paper);
		return buildPrompt("writing.论文格式化提示词", variables);
	}

	/**
	 * 获取摘要生成提示词
	 */
	public String getAbstractGenerationPrompt(String problemStatement, Object modelingResult, Object codingResult) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemStatement", problemStatement);
		variables.put("modelingResult", modelingResult);
		variables.put("codingResult", codingResult);
		return buildPrompt("writing.摘要生成提示词", variables);
	}

	/**
	 * 获取关键词生成提示词
	 */
	public String getKeywordsGenerationPrompt(Object modelingResult) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("modelingResult", modelingResult);
		return buildPrompt("writing.关键词生成提示词", variables);
	}

	/**
	 * 获取引言撰写提示词
	 */
	public String getIntroductionPrompt(String researchTopic, String problemBackground) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("researchTopic", researchTopic);
		variables.put("problemBackground", problemBackground);
		return buildPrompt("writing.引言撰写提示词", variables);
	}

	/**
	 * 获取结论撰写提示词
	 */
	public String getConclusionPrompt(Object researchResults, Object modelEvaluation) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("researchResults", researchResults);
		variables.put("modelEvaluation", modelEvaluation);
		return buildPrompt("writing.结论撰写提示词", variables);
	}

	// ========== 通用提示词 ==========

	/**
	 * 获取错误处理提示词
	 */
	public String getErrorHandlingPrompt(String error, String context) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("error", error);
		variables.put("context", context);
		return buildPrompt("common.错误处理提示词", variables);
	}

	/**
	 * 获取结果验证提示词
	 */
	public String getResultValidationPrompt(Object result) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("result", result);
		return buildPrompt("common.结果验证提示词", variables);
	}

	/**
	 * 获取质量评估提示词
	 */
	public String getQualityAssessmentPrompt(Object workContent) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("workContent", workContent);
		return buildPrompt("common.质量评估提示词", variables);
	}

	/**
	 * 获取优化建议提示词
	 */
	public String getOptimizationSuggestionPrompt(Object currentContent, String optimizationGoal) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("currentContent", currentContent);
		variables.put("optimizationGoal", optimizationGoal);
		return buildPrompt("common.优化建议提示词", variables);
	}

	/**
	 * 获取总结生成提示词
	 */
	public String getSummaryGenerationPrompt(Object originalContent) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("originalContent", originalContent);
		return buildPrompt("common.总结生成提示词", variables);
	}

	/**
	 * 获取格式转换提示词
	 */
	public String getFormatConversionPrompt(Object originalContent, String targetFormat) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("originalContent", originalContent);
		variables.put("targetFormat", targetFormat);
		return buildPrompt("common.格式转换提示词", variables);
	}

	/**
	 * 重新加载提示词
	 */
	public void reloadPrompts() {
		initialized = false;
		promptTemplates.clear();
		initialize();
		log.info("提示词重新加载完成");
	}

	/**
	 * 获取所有提示词名称
	 */
	public Map<String, String> getAllPromptTemplates() {
		if (!initialized) {
			initialize();
		}
		return new HashMap<>(promptTemplates);
	}

	/**
	 * 获取指定Agent的提示词
	 */
	public Map<String, String> getAgentPrompts(String agentName) {
		if (!initialized) {
			initialize();
		}

		Map<String, String> agentPrompts = new HashMap<>();
		String prefix = agentName + ".";

		for (Map.Entry<String, String> entry : promptTemplates.entrySet()) {
			if (entry.getKey().startsWith(prefix)) {
				String promptName = entry.getKey().substring(prefix.length());
				agentPrompts.put(promptName, entry.getValue());
			}
		}

		return agentPrompts;
	}

	// ========== Nodes包提示词 ==========

	/**
	 * 获取数据收集提示词
	 */
	public String getDataCollectionPrompt(Map<String, Object> problemAnalysis) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemAnalysis", problemAnalysis);
		return buildPrompt("nodes.数据收集提示词", variables);
	}

	/**
	 * 获取模型构建提示词
	 */
	public String getModelBuildingPrompt(Map<String, Object> problemAnalysis, Map<String, Object> collectedData) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemAnalysis", problemAnalysis);
		variables.put("collectedData", collectedData);
		return buildPrompt("nodes.模型构建提示词", variables);
	}

	/**
	 * 获取模型求解提示词
	 */
	public String getModelSolvingPrompt(Map<String, Object> problemAnalysis, Map<String, Object> modelDefinition) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("problemAnalysis", problemAnalysis);
		variables.put("modelDefinition", modelDefinition);
		return buildPrompt("nodes.模型求解提示词", variables);
	}

}
