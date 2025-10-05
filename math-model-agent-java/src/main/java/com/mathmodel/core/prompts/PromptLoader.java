package com.mathmodel.core.prompts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Prompt Loader - Loads prompts from external files
 * 提示词加载器 - 从外部文件加载提示词
 */
@Slf4j
@Component
public class PromptLoader {

    private static final String PROMPTS_DIR = "prompts";
    private final Map<String, String> promptCache = new HashMap<>();

    /**
     * Prompt file names
     */
    public static final String COORDINATOR_PROMPT = "coordinator.txt";
    public static final String MODELER_PROMPT = "modeler.txt";
    public static final String CODER_PROMPT = "coder.txt";
    public static final String WRITER_PROMPT = "writer.txt";
    public static final String REFLECTION_PROMPT = "reflection.txt";

    @PostConstruct
    public void init() {
        log.info("Initializing PromptLoader...");
        loadAllPrompts();
    }

    /**
     * Load all prompts at startup
     */
    private void loadAllPrompts() {
        loadPrompt(COORDINATOR_PROMPT);
        loadPrompt(MODELER_PROMPT);
        loadPrompt(CODER_PROMPT);
        loadPrompt(WRITER_PROMPT);
        loadPrompt(REFLECTION_PROMPT);
        log.info("Loaded {} prompts successfully", promptCache.size());
    }

    /**
     * Load a specific prompt file
     */
    private void loadPrompt(String fileName) {
        try {
            String promptPath = PROMPTS_DIR + "/" + fileName;
            ClassPathResource resource = new ClassPathResource(promptPath);
            
            if (!resource.exists()) {
                log.warn("Prompt file not found: {}", promptPath);
                return;
            }

            String content = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            
            promptCache.put(fileName, content.trim());
            log.debug("Loaded prompt: {}", fileName);
            
        } catch (IOException e) {
            log.error("Failed to load prompt file: {}", fileName, e);
        }
    }

    /**
     * Get coordinator prompt
     */
    public String getCoordinatorPrompt() {
        return getPrompt(COORDINATOR_PROMPT);
    }

    /**
     * Get modeler prompt
     */
    public String getModelerPrompt() {
        return getPrompt(MODELER_PROMPT);
    }

    /**
     * Get coder prompt
     */
    public String getCoderPrompt() {
        return getPrompt(CODER_PROMPT);
    }

    /**
     * Get writer prompt
     */
    public String getWriterPrompt() {
        return getPrompt(WRITER_PROMPT);
    }

    /**
     * Get reflection prompt with parameters
     */
    public String getReflectionPrompt(String errorMessage, int retryCount) {
        String template = getPrompt(REFLECTION_PROMPT);
        return template
                .replace("{error_message}", errorMessage)
                .replace("{retry_count}", String.valueOf(retryCount));
    }

    /**
     * Get prompt by file name
     */
    public String getPrompt(String fileName) {
        String prompt = promptCache.get(fileName);
        if (prompt == null) {
            log.warn("Prompt not found in cache, attempting to reload: {}", fileName);
            loadPrompt(fileName);
            prompt = promptCache.get(fileName);
        }
        
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt file not found: " + fileName);
        }
        
        return prompt;
    }

    /**
     * Reload a specific prompt (useful for hot-reload)
     */
    public void reloadPrompt(String fileName) {
        log.info("Reloading prompt: {}", fileName);
        loadPrompt(fileName);
    }

    /**
     * Reload all prompts
     */
    public void reloadAllPrompts() {
        log.info("Reloading all prompts...");
        promptCache.clear();
        loadAllPrompts();
    }

    /**
     * Get all loaded prompt names
     */
    public Map<String, String> getAllPrompts() {
        return new HashMap<>(promptCache);
    }

    /**
     * Check if a prompt is loaded
     */
    public boolean isPromptLoaded(String fileName) {
        return promptCache.containsKey(fileName);
    }
}
