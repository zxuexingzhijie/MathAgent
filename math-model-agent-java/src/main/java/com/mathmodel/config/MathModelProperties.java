package com.mathmodel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Math Model Configuration Properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "math-model")
public class MathModelProperties {

    /**
     * Working directory for storing project files
     */
    private String workDir = "./project/work_dir";

    /**
     * Maximum chat turns
     */
    private int maxChatTurns = 30;

    /**
     * Maximum retries
     */
    private int maxRetries = 3;

    /**
     * Model configurations for different agents
     */
    private Models models = new Models();

    /**
     * Code interpreter configuration
     */
    private CodeInterpreter codeInterpreter = new CodeInterpreter();

    /**
     * Scholar configuration
     */
    private Scholar scholar = new Scholar();

    @Data
    public static class Models {
        private ModelConfig coordinator = new ModelConfig();
        private ModelConfig modeler = new ModelConfig();
        private ModelConfig coder = new ModelConfig();
        private ModelConfig writer = new ModelConfig();
    }

    @Data
    public static class ModelConfig {
        private String model = "qwen-max";
        private Double temperature = 0.7;
        private Integer maxTokens = 4000;
    }

    @Data
    public static class CodeInterpreter {
        private String type = "local"; // local or e2b
        private Integer timeout = 300;
        private String kernelName = "python3";
        private E2bConfig e2b = new E2bConfig();
    }

    @Data
    public static class E2bConfig {
        private String apiKey;
    }

    @Data
    public static class Scholar {
        private OpenAlexConfig openalex = new OpenAlexConfig();
    }

    @Data
    public static class OpenAlexConfig {
        private String email;
        private Integer perPage = 10;
        private Integer maxResults = 20;
    }
}
