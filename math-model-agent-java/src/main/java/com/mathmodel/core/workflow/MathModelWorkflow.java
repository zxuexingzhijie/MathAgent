package com.mathmodel.core.workflow;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.mathmodel.config.MathModelProperties;
import com.mathmodel.core.agent.CoderAgent;
import com.mathmodel.core.agent.CoordinatorAgent;
import com.mathmodel.core.agent.ModelerAgent;
import com.mathmodel.core.agent.WriterAgent;
import com.mathmodel.core.interpreter.CodeInterpreter;
import com.mathmodel.core.prompts.PromptLoader;
import com.mathmodel.schema.a2a.CoderToWriter;
import com.mathmodel.schema.a2a.CoordinatorToModeler;
import com.mathmodel.schema.a2a.ModelerToCoder;
import com.mathmodel.schema.request.ProblemRequest;
import com.mathmodel.schema.response.SystemMessage;
import com.mathmodel.service.ScholarService;
import com.mathmodel.service.WebSocketService;
import com.mathmodel.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    private final CodeInterpreter codeInterpreter;
    private final ScholarService scholarService;

    public MathModelWorkflow(
            MathModelProperties properties,
            WebSocketService webSocketService,
            ModelFactory modelFactory,
            PromptLoader promptLoader,
            CodeInterpreter codeInterpreter,
            ScholarService scholarService) {
        this.properties = properties;
        this.webSocketService = webSocketService;
        this.modelFactory = modelFactory;
        this.promptLoader = promptLoader;
        this.codeInterpreter = codeInterpreter;
        this.scholarService = scholarService;
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

        sendMessage(taskId, SystemMessage.success("建模完成，任务转交给代码手"));

        // Step 3: Coder Agent - Write and execute code
        sendMessage(taskId, SystemMessage.info("代码手开始编写和执行代码ing..."));

        DashScopeChatModel coderModel = modelFactory.createCoderModel();
        CoderAgent coderAgent = new CoderAgent(
                taskId,
                coderModel,
                properties.getMaxChatTurns(),
                promptLoader,
                codeInterpreter
        );

        CoderToWriter coderResponse;
        try {
            coderResponse = (CoderToWriter) coderAgent.run(modelerResponse);
            log.info("[Workflow] Coder completed. Generated {} images",
                    coderResponse.getCreatedImages() != null ? coderResponse.getCreatedImages().size() : 0);
        } catch (Exception e) {
            log.error("[Workflow] Coder failed: {}", e.getMessage());
            sendMessage(taskId, SystemMessage.error("代码执行失败: " + e.getMessage()));
            throw e;
        }

        sendMessage(taskId, SystemMessage.success("代码执行完成，任务转交给论文手"));

        // Step 4: Writer Agent - Generate paper
        sendMessage(taskId, SystemMessage.info("论文手开始撰写论文ing..."));

        DashScopeChatModel writerModel = modelFactory.createWriterModel();
        WriterAgent writerAgent = new WriterAgent(
                taskId,
                writerModel,
                properties.getMaxChatTurns(),
                promptLoader,
                scholarService,
                problem.getCompTemplate(),
                problem.getFormatOutput()
        );

        String paper;
        try {
            paper = (String) writerAgent.run(coderResponse);
            log.info("[Workflow] Writer completed. Paper length: {} characters", paper.length());
        } catch (Exception e) {
            log.error("[Workflow] Writer failed: {}", e.getMessage());
            sendMessage(taskId, SystemMessage.error("论文撰写失败: " + e.getMessage()));
            throw e;
        }

        // Save paper to file
        try {
            Path outputDir = workDir.resolve("output");
            String extension = problem.getFormatOutput().getValue().equals("latex") ? ".tex" : ".md";
            Path paperPath = outputDir.resolve("paper" + extension);
            Files.writeString(paperPath, paper, StandardCharsets.UTF_8);
            log.info("[Workflow] Paper saved to: {}", paperPath);
            sendMessage(taskId, SystemMessage.success("论文已保存至: " + paperPath.getFileName()));
        } catch (Exception e) {
            log.error("[Workflow] Failed to save paper", e);
            sendMessage(taskId, SystemMessage.warning("论文保存失败，但内容已生成"));
        }

        // Final step
        sendMessage(taskId, SystemMessage.success("✅ 工作流程全部完成！"));
        log.info("[Workflow] Workflow completed successfully for task: {}", taskId);
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
