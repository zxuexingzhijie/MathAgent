package com.mathagent.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 任务结果实体
 * 存储每个任务节点的执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResult {
    
    private Long id;
    private Long taskId;
    private String nodeName;
    private NodeStatus status;
    private String inputData;
    private String outputData;
    private String errorMessage;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum NodeStatus {
        PENDING("等待中"),
        RUNNING("执行中"),
        SUCCESS("成功"),
        FAILED("失败"),
        SKIPPED("跳过");
        
        private final String description;
        
        NodeStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}