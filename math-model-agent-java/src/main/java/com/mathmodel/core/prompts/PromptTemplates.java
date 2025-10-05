package com.mathmodel.core.prompts;

/**
 * Prompt Templates for Different Agents
 * 
 * @deprecated This class is deprecated and will be removed in future versions.
 *             Please use {@link PromptLoader} instead for loading prompts from external files.
 *             
 *             已弃用：此类已过时，将在未来版本中移除。
 *             请使用 {@link PromptLoader} 从外部文件加载提示词。
 *             
 * Migration guide:
 * - Old: PromptTemplates.COORDINATOR_PROMPT
 * - New: promptLoader.getCoordinatorPrompt()
 * 
 * Benefits of using PromptLoader:
 * - Prompts are stored in external files (src/main/resources/prompts/)
 * - Easy to modify without recompiling code
 * - Supports hot-reload
 * - Follows Single Responsibility Principle
 * - Better maintainability
 */
@Deprecated(since = "1.0.0", forRemoval = true)
public class PromptTemplates {

    private PromptTemplates() {
        throw new UnsupportedOperationException("This class is deprecated. Use PromptLoader instead.");
    }

    /**
     * @deprecated Use {@link PromptLoader#getCoordinatorPrompt()} instead.
     *             Prompt is now loaded from src/main/resources/prompts/coordinator.txt
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String COORDINATOR_PROMPT = 
            "DEPRECATED: Use PromptLoader.getCoordinatorPrompt() instead";

    /**
     * @deprecated Use {@link PromptLoader#getModelerPrompt()} instead.
     *             Prompt is now loaded from src/main/resources/prompts/modeler.txt
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String MODELER_PROMPT = 
            "DEPRECATED: Use PromptLoader.getModelerPrompt() instead";

    /**
     * @deprecated Use {@link PromptLoader#getCoderPrompt()} instead.
     *             Prompt is now loaded from src/main/resources/prompts/coder.txt
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String CODER_PROMPT = 
            "DEPRECATED: Use PromptLoader.getCoderPrompt() instead";

    /**
     * @deprecated Use {@link PromptLoader#getWriterPrompt()} instead.
     *             Prompt is now loaded from src/main/resources/prompts/writer.txt
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String WRITER_PROMPT = 
            "DEPRECATED: Use PromptLoader.getWriterPrompt() instead";

    /**
     * @deprecated Use {@link PromptLoader#getReflectionPrompt(String, int)} instead.
     *             Prompt is now loaded from src/main/resources/prompts/reflection.txt
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static final String REFLECTION_PROMPT_TEMPLATE = 
            "DEPRECATED: Use PromptLoader.getReflectionPrompt(errorMessage, retryCount) instead";

    /**
     * @deprecated Use {@link PromptLoader#getReflectionPrompt(String, int)} instead.
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    public static String getReflectionPrompt(String errorMessage, int retryCount) {
        throw new UnsupportedOperationException(
                "This method is deprecated. Use PromptLoader.getReflectionPrompt(errorMessage, retryCount) instead."
        );
    }
}
