package com.mathmodel.core.workflow;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.mathmodel.config.MathModelProperties;
import com.mathmodel.core.agent.CoordinatorAgent;
import com.mathmodel.core.agent.ModelerAgent;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoordinatorToModeler;
import com.mathmodel.schema.a2a.ModelerToCoder;
import com.mathmodel.schema.request.ProblemRequest;
import com.mathmodel.schema.response.SystemMessage;
import com.mathmodel.service.WebSocketService;
import com.mathmodel.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Math Model Workflow
 * Orchestrates the entire mathematical modeling process
 */
@Slf4j
@Component
public class MathModelWorkflow {

    private final MathModelProperties properties;
    private final WebSocketService webSocketService;
    private final ModelFactory modelFactory;
    private final PromptLoader promptLoader;

    public MathModelWorkflow(
            MathModelProperties properties,
            WebSocketService webSocketService,
            ModelFactory modelFactory,
            PromptLoader promptLoader) {
        this.properties = properties;
        this.webSocketService = webSocketService;
        this.modelFactory = modelFactory;
        this.promptLoader = promptLoader;
    }

    /**
     * Execute the mathematical modeling workflow
     */
    public void execute(ProblemRequest problem) throws Exception {
        String taskId = problem.getTaskId();
        log.info("[Workflow] Starting workflow for task: {}", taskId);

        // Create working directory
        Path workDir = createWorkDir(taskId);
        log.info("[Workflow] Working directory created: {}", workDir);

        // Get chat models for different agents
        DashScopeChatModel coordinatorModel = modelFactory.createCoordinatorModel();
        DashScopeChatModel modelerModel = modelFactory.createModelerModel();

        // Step 1: Coordinator Agent - Analyze and decompose problem
        sendMessage(taskId, SystemMessage.info("识别用户意图和拆解问题ing..."));

        CoordinatorAgent coordinatorAgent = new CoordinatorAgent(
                taskId,
                coordinatorModel,
                properties.getMaxChatTurns(),
                promptLoader
        );

        CoordinatorToModeler coordinatorResponse;
        try {
            coordinatorResponse = (CoordinatorToModeler) coordinatorAgent.run(problem.getQuesAll());
            log.info("[Workflow] Coordinator completed. Found {} questions", 
                    coordinatorResponse.getQuesCount());
        } catch (Exception e) {
            log.error("[Workflow] Coordinator failed: {}", e.getMessage());
            sendMessage(taskId, SystemMessage.error("问题识别失败: " + e.getMessage()));
            throw e;
        }

        sendMessage(taskId, SystemMessage.success("识别用户意图和拆解问题完成,任务转交给建模手"));

        // Step 2: Modeler Agent - Create mathematical models
        sendMessage(taskId, SystemMessage.info("建模手开始建模ing..."));

        ModelerAgent modelerAgent = new ModelerAgent(
                taskId,
                modelerModel,
                properties.getMaxChatTurns(),
                promptLoader
        );

        ModelerToCoder modelerResponse;
        try {
            modelerResponse = (ModelerToCoder) modelerAgent.run(coordinatorResponse);
            log.info("[Workflow] Modeler completed. Created {} models", 
                    modelerResponse.getQuestionsSolution().size());
        } catch (Exception e) {
            log.error("[Workflow] Modeler failed: {}", e.getMessage());
            sendMessage(taskId, SystemMessage.error("建模失败: " + e.getMessage()));
            throw e;
        }

        sendMessage(taskId, SystemMessage.success("建模完成"));

        // Step 3: Coder Agent - Write and execute code
        sendMessage(taskId, SystemMessage.info("代码手准备开始编码..."));
        
        // TODO: Implement CoderAgent
        log.info("[Workflow] Coder agent execution - to be implemented");

        // Step 4: Writer Agent - Generate paper
        sendMessage(taskId, SystemMessage.info("论文手准备开始撰写..."));
        
        // TODO: Implement WriterAgent
        log.info("[Workflow] Writer agent execution - to be implemented");

        // Final step
        sendMessage(taskId, SystemMessage.success("工作流程完成！"));
        log.info("[Workflow] Workflow completed for task: {}", taskId);
    }

    /**
     * Create working directory for the task
     */
    private Path createWorkDir(String taskId) {
        Path baseDir = Paths.get(properties.getWorkDir());
        Path taskDir = baseDir.resolve(taskId);
        
        try {
            FileUtils.createDirectories(taskDir);
            
            // Create subdirectories
            FileUtils.createDirectories(taskDir.resolve("data"));
            FileUtils.createDirectories(taskDir.resolve("code"));
            FileUtils.createDirectories(taskDir.resolve("images"));
            FileUtils.createDirectories(taskDir.resolve("output"));
            
            return taskDir;
        } catch (Exception e) {
            log.error("[Workflow] Failed to create working directory", e);
            throw new RuntimeException("Failed to create working directory: " + e.getMessage(), e);
        }
    }

    /**
     * Send message through WebSocket
     */
    private void sendMessage(String taskId, SystemMessage message) {
        try {
            webSocketService.sendMessage(taskId, message);
        } catch (Exception e) {
            log.warn("[Workflow] Failed to send WebSocket message", e);
        }
    }
}
