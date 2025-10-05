package com.mathmodel.controller;

import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Prompt Management Controller
 * 提示词管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/prompts")
public class PromptAdminController {

    private final PromptLoader promptLoader;

    @Autowired
    public PromptAdminController(PromptLoader promptLoader) {
        this.promptLoader = promptLoader;
    }

    /**
     * Get all loaded prompts
     * 获取所有已加载的提示词
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Map<String, String>>> listPrompts() {
        try {
            Map<String, String> prompts = promptLoader.getAllPrompts();
            return ResponseEntity.ok(ApiResponse.success("Prompts retrieved successfully", prompts));
        } catch (Exception e) {
            log.error("Failed to list prompts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to list prompts: " + e.getMessage()));
        }
    }

    /**
     * Get a specific prompt by name
     * 获取指定名称的提示词
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<ApiResponse<String>> getPrompt(@PathVariable String fileName) {
        try {
            // Add .txt extension if not present
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            
            String prompt = promptLoader.getPrompt(fileName);
            return ResponseEntity.ok(ApiResponse.success("Prompt retrieved successfully", prompt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to get prompt: {}", fileName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get prompt: " + e.getMessage()));
        }
    }

    /**
     * Reload a specific prompt
     * 重新加载指定的提示词
     */
    @PostMapping("/reload/{fileName}")
    public ResponseEntity<ApiResponse<String>> reloadPrompt(@PathVariable String fileName) {
        try {
            // Add .txt extension if not present
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            
            promptLoader.reloadPrompt(fileName);
            log.info("Prompt reloaded: {}", fileName);
            return ResponseEntity.ok(ApiResponse.success("Prompt reloaded successfully", fileName));
        } catch (Exception e) {
            log.error("Failed to reload prompt: {}", fileName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to reload prompt: " + e.getMessage()));
        }
    }

    /**
     * Reload all prompts
     * 重新加载所有提示词
     */
    @PostMapping("/reload-all")
    public ResponseEntity<ApiResponse<String>> reloadAllPrompts() {
        try {
            promptLoader.reloadAllPrompts();
            log.info("All prompts reloaded");
            return ResponseEntity.ok(ApiResponse.success("All prompts reloaded successfully"));
        } catch (Exception e) {
            log.error("Failed to reload all prompts", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to reload all prompts: " + e.getMessage()));
        }
    }

    /**
     * Check if a prompt is loaded
     * 检查提示词是否已加载
     */
    @GetMapping("/check/{fileName}")
    public ResponseEntity<ApiResponse<Boolean>> checkPrompt(@PathVariable String fileName) {
        try {
            // Add .txt extension if not present
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
            }
            
            boolean loaded = promptLoader.isPromptLoaded(fileName);
            return ResponseEntity.ok(ApiResponse.success("Prompt check completed", loaded));
        } catch (Exception e) {
            log.error("Failed to check prompt: {}", fileName, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check prompt: " + e.getMessage()));
        }
    }

    /**
     * Get prompt statistics
     * 获取提示词统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        try {
            Map<String, String> prompts = promptLoader.getAllPrompts();
            Map<String, Object> stats = Map.of(
                    "total_prompts", prompts.size(),
                    "prompt_names", prompts.keySet(),
                    "total_characters", prompts.values().stream()
                            .mapToInt(String::length)
                            .sum()
            );
            return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", stats));
        } catch (Exception e) {
            log.error("Failed to get prompt stats", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to get stats: " + e.getMessage()));
        }
    }
}
