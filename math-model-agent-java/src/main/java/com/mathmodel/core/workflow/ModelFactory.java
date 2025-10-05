package com.mathmodel.core.workflow;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.mathmodel.config.MathModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory for creating chat models with different configurations
 */
@Slf4j
@Component
public class ModelFactory {

    private final DashScopeChatModel baseChatModel;
    private final MathModelProperties properties;

    @Autowired
    public ModelFactory(ChatModel chatModel, MathModelProperties properties) {
        this.baseChatModel = (DashScopeChatModel) chatModel;
        this.properties = properties;
    }

    /**
     * Create coordinator model
     */
    public DashScopeChatModel createCoordinatorModel() {
        MathModelProperties.ModelConfig config = properties.getModels().getCoordinator();
        return createModel(config);
    }

    /**
     * Create modeler model
     */
    public DashScopeChatModel createModelerModel() {
        MathModelProperties.ModelConfig config = properties.getModels().getModeler();
        return createModel(config);
    }

    /**
     * Create coder model
     */
    public DashScopeChatModel createCoderModel() {
        MathModelProperties.ModelConfig config = properties.getModels().getCoder();
        return createModel(config);
    }

    /**
     * Create writer model
     */
    public DashScopeChatModel createWriterModel() {
        MathModelProperties.ModelConfig config = properties.getModels().getWriter();
        return createModel(config);
    }

    /**
     * Create model with specific configuration
     */
    private DashScopeChatModel createModel(MathModelProperties.ModelConfig config) {
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(config.getModel())
                .withTemperature(config.getTemperature())
                .withMaxTokens(config.getMaxTokens())
                .build();

        // Clone the base model with new options
        log.debug("Creating model with config: model={}, temperature={}, maxTokens={}", 
                config.getModel(), config.getTemperature(), config.getMaxTokens());
        
        return new DashScopeChatModel(baseChatModel.getDefaultOptions().toBuilder()
                .withModel(config.getModel())
                .withTemperature(config.getTemperature())
                .withMaxTokens(config.getMaxTokens())
                .build());
    }
}
