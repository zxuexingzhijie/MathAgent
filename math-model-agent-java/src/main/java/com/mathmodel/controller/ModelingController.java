package com.mathmodel.controller;

import com.mathmodel.core.workflow.MathModelWorkflow;
import com.mathmodel.schema.request.ProblemRequest;
import com.mathmodel.schema.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Modeling Controller
 * Handles mathematical modeling requests
 */
@Slf4j
@RestController
@RequestMapping("/modeling")
public class ModelingController {

    private final MathModelWorkflow workflow;

    @Autowired
    public ModelingController(MathModelWorkflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Submit a mathematical modeling problem
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<String>> submitProblem(@Valid @RequestBody ProblemRequest request) {
        try {
            log.info("Received modeling request for task: {}", request.getTaskId());

            // Execute workflow asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    workflow.execute(request);
                } catch (Exception e) {
                    log.error("Workflow execution failed for task: {}", request.getTaskId(), e);
                }
            });

            return ResponseEntity.ok(ApiResponse.success(
                    "Task submitted successfully",
                    request.getTaskId()
            ));

        } catch (Exception e) {
            log.error("Failed to submit modeling request", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to submit task: " + e.getMessage()));
        }
    }

    /**
     * Generate a new task ID
     */
    @GetMapping("/generate-task-id")
    public ResponseEntity<ApiResponse<String>> generateTaskId() {
        String taskId = UUID.randomUUID().toString();
        log.info("Generated new task ID: {}", taskId);
        return ResponseEntity.ok(ApiResponse.success(taskId));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }
}
