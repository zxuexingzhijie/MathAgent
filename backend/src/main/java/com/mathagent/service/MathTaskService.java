package com.mathagent.service;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.mathagent.mapper.MathTaskMapper;
import com.mathagent.mapper.TaskResultMapper;
import com.mathagent.mapper.TaskLogMapper;
import com.mathagent.model.MathTask;
import com.mathagent.model.TaskResult;
import com.mathagent.model.TaskLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 数学建模任务服务
 * 使用MyBatis进行数据访问
 */
@Slf4j
@Service
@Transactional
public class MathTaskService {

    @Autowired
    private MathTaskMapper mathTaskMapper;

    @Autowired
    private TaskResultMapper taskResultMapper;

    @Autowired
    private TaskLogMapper taskLogMapper;

    @Autowired
    @Qualifier("compiledMathModelingGraph")
    private CompiledGraph compiledGraph;

    /**
     * 创建任务
     */
    public MathTask createTask(MathTask task) {
        log.info("创建数学建模任务: {}", task.getTitle());
        
        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus(MathTask.TaskStatus.CREATED);
        }
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // 插入数据库
        mathTaskMapper.insert(task);
        
        // 记录日志
        addTaskLog(task.getId(), "SYSTEM", TaskLog.LogLevel.INFO, 
                  "任务创建成功", "任务已创建并等待执行");
        
        log.info("任务创建成功，ID: {}", task.getId());
        return task;
    }

    /**
     * 执行任务
     */
    public void executeTask(Long taskId) {
        log.info("开始执行任务: {}", taskId);
        
        MathTask task = mathTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
        
        try {
            // 更新任务状态
            updateTaskStatus(taskId, MathTask.TaskStatus.ANALYZING);
            
            // 构建Graph状态
            OverAllState state = new OverAllState();
            state.putValue("input", task.getProblemStatement());
            state.putValue("task_id", taskId.toString());
            
            // 执行Graph工作流
            OverAllState result = compiledGraph.invoke(state);
            
            // 更新任务完成状态
            updateTaskStatus(taskId, MathTask.TaskStatus.COMPLETED);
            updateTaskCompletionTime(taskId);
            
            log.info("任务执行完成: {}", taskId);
            
        } catch (Exception e) {
            log.error("任务执行失败: {}", taskId, e);
            updateTaskStatus(taskId, MathTask.TaskStatus.FAILED);
            addTaskLog(taskId, "SYSTEM", TaskLog.LogLevel.ERROR, 
                      "任务执行失败", e.getMessage());
            throw new RuntimeException("任务执行失败", e);
        }
    }

    /**
     * 获取任务列表
     */
    public List<MathTask> getAllTasks() {
        return mathTaskMapper.selectAll();
    }

    /**
     * 根据状态获取任务
     */
    public List<MathTask> getTasksByStatus(MathTask.TaskStatus status) {
        return mathTaskMapper.selectByStatus(status);
    }

    /**
     * 根据类型获取任务
     */
    public List<MathTask> getTasksByType(MathTask.TaskType type) {
        return mathTaskMapper.selectByType(type);
    }

    /**
     * 根据标题搜索任务
     */
    public List<MathTask> searchTasksByTitle(String title) {
        return mathTaskMapper.selectByTitleContaining(title);
    }

    /**
     * 获取任务详情
     */
    public MathTask getTaskById(Long taskId) {
        return mathTaskMapper.selectById(taskId);
    }

    /**
     * 获取任务结果
     */
    public List<TaskResult> getTaskResults(Long taskId) {
        return taskResultMapper.selectByTaskId(taskId);
    }

    /**
     * 获取任务日志
     */
    public List<TaskLog> getTaskLogs(Long taskId) {
        return taskLogMapper.selectByTaskId(taskId);
    }

    /**
     * 更新任务状态
     */
    public void updateTaskStatus(Long taskId, MathTask.TaskStatus status) {
        mathTaskMapper.updateStatus(taskId, status);
        addTaskLog(taskId, "SYSTEM", TaskLog.LogLevel.INFO, 
                  "状态更新", "任务状态更新为: " + status.getDescription());
    }

    /**
     * 更新任务完成时间
     */
    public void updateTaskCompletionTime(Long taskId) {
        MathTask task = mathTaskMapper.selectById(taskId);
        if (task != null) {
            task.setCompletedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            mathTaskMapper.update(task);
        }
    }

    /**
     * 添加任务日志
     */
    public void addTaskLog(Long taskId, String nodeName, TaskLog.LogLevel level, 
                          String message, String details) {
        TaskLog log = TaskLog.builder()
                .taskId(taskId)
                .nodeName(nodeName)
                .level(level)
                .message(message)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        taskLogMapper.insert(log);
    }

    /**
     * 保存任务结果
     */
    public void saveTaskResult(Long taskId, String nodeName, TaskResult.NodeStatus status,
                              String inputData, String outputData, String errorMessage) {
        TaskResult result = TaskResult.builder()
                .taskId(taskId)
                .nodeName(nodeName)
                .status(status)
                .inputData(inputData)
                .outputData(outputData)
                .errorMessage(errorMessage)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        taskResultMapper.insert(result);
    }

    /**
     * 获取任务统计信息
     */
    public Map<String, Object> getTaskStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计各状态任务数量
        List<Object[]> statusCounts = mathTaskMapper.countByStatus();
        Map<String, Long> statusStats = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("statusCounts", statusStats);
        
        // 总任务数
        List<MathTask> allTasks = mathTaskMapper.selectAll();
        stats.put("totalTasks", allTasks.size());
        
        return stats;
    }

    /**
     * 删除任务
     */
    public void deleteTask(Long taskId) {
        log.info("删除任务: {}", taskId);
        
        // 删除相关日志和结果
        taskLogMapper.deleteByTaskId(taskId);
        taskResultMapper.deleteByTaskId(taskId);
        
        // 删除任务
        mathTaskMapper.deleteById(taskId);
        
        log.info("任务删除成功: {}", taskId);
    }
}