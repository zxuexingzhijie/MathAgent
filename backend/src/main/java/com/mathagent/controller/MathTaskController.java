package com.mathagent.controller;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.mathagent.model.MathTask;
import com.mathagent.model.TaskResult;
import com.mathagent.model.TaskLog;
import com.mathagent.service.MathTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.alibaba.cloud.ai.graph.OverAllState.DEFAULT_INPUT_KEY;

/**
 * 数学建模任务控制器 提供任务管理和执行API
 * 
 * @author Makoto
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class MathTaskController {

	@Autowired
	private MathTaskService mathTaskService;

	@Autowired
	@Qualifier("compiledMathModelingGraph")
	private CompiledGraph compiledGraph;

	/**
	 * 创建新的数学建模任务
	 */
	@PostMapping
	public ResponseEntity<MathTask> createTask(@RequestBody CreateTaskRequest request) {
		log.info("创建新任务: {}", request.getTitle());

		MathTask task = MathTask.builder()
			.title(request.getTitle())
			.description(request.getDescription())
			.problemStatement(request.getProblemStatement())
			.researchGoals(request.getResearchGoals())
			.dataRequirements(request.getDataRequirements())
			.modelConstraints(request.getModelConstraints())
			.expectedOutputs(request.getExpectedOutputs())
			.type(request.getType())
			.status(MathTask.TaskStatus.CREATED)
			.build();

		MathTask savedTask = mathTaskService.createTask(task);
		return ResponseEntity.ok(savedTask);
	}

	/**
	 * 执行数学建模任务
	 */
	@PostMapping("/{taskId}/execute")
	public ResponseEntity<Map<String, Object>> executeTask(@PathVariable Long taskId) {
		log.info("执行任务: {}", taskId);

		try {
			MathTask task = mathTaskService.getTaskById(taskId);
			if (task == null) {
				throw new RuntimeException("任务不存在");
			}

			// 更新任务状态
			task.setStatus(MathTask.TaskStatus.ANALYZING);
			task.setStartedAt(LocalDateTime.now());
			mathTaskService.updateTask(task);

			// 记录开始日志
			mathTaskService.addTaskLog(task.getId(), "GRAPH_EXECUTOR", TaskLog.LogLevel.INFO, "开始执行数学建模Graph工作流", null);

			// 异步执行Graph工作流
			String executionId = executeGraphWorkflow(task);

			task.setGraphExecutionId(executionId);
			mathTaskService.updateTask(task);

			return ResponseEntity.ok(Map.of("taskId", taskId, "executionId", executionId, "status",
					task.getStatus().name(), "message", "任务执行已开始"));

		}
		catch (Exception e) {
			log.error("执行任务失败", e);
			return ResponseEntity.badRequest().body(Map.of("error", "执行任务失败: " + e.getMessage()));
		}
	}

	/**
	 * 获取任务列表
	 */
	@GetMapping
	public ResponseEntity<List<MathTask>> getTasks() {
		List<MathTask> tasks = mathTaskService.getAllTasks();
		return ResponseEntity.ok(tasks);
	}

	/**
	 * 获取任务详情
	 */
	@GetMapping("/{taskId}")
	public ResponseEntity<MathTask> getTask(@PathVariable Long taskId) {
		MathTask task = mathTaskService.getTaskById(taskId);
		if (task == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(task);
	}

	/**
	 * 获取任务结果
	 */
	@GetMapping("/{taskId}/results")
	public ResponseEntity<List<TaskResult>> getTaskResults(@PathVariable Long taskId) {
		List<TaskResult> results = mathTaskService.getTaskResults(taskId);
		return ResponseEntity.ok(results);
	}

	/**
	 * 获取任务日志
	 */
	@GetMapping("/{taskId}/logs")
	public ResponseEntity<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
		List<TaskLog> logs = mathTaskService.getTaskLogs(taskId);
		return ResponseEntity.ok(logs);
	}

	/**
	 * 取消任务
	 */
	@PostMapping("/{taskId}/cancel")
	public ResponseEntity<Map<String, Object>> cancelTask(@PathVariable Long taskId) {
		try {
			MathTask task = mathTaskService.getTaskById(taskId);
			if (task == null) {
				throw new RuntimeException("任务不存在");
			}

			task.setStatus(MathTask.TaskStatus.CANCELLED);
			task.setCompletedAt(LocalDateTime.now());
			mathTaskService.updateTask(task);

			mathTaskService.addTaskLog(task.getId(), "USER", TaskLog.LogLevel.INFO, "任务已被用户取消", null);

			return ResponseEntity.ok(Map.of("taskId", taskId, "status", task.getStatus().name(), "message", "任务已取消"));

		}
		catch (Exception e) {
			log.error("取消任务失败", e);
			return ResponseEntity.badRequest().body(Map.of("error", "取消任务失败: " + e.getMessage()));
		}
	}

	/**
	 * 执行Graph工作流
	 */
	private String executeGraphWorkflow(MathTask task) {
		try {
			// 构建输入状态
			Map<String, Object> input = Map.of(DEFAULT_INPUT_KEY, task.getProblemStatement(), "task_id", task.getId(),
					"task_title", task.getTitle());

			// 执行Graph工作流
			Optional<OverAllState> result = compiledGraph.call(input);

			if (result.isPresent()) {
				OverAllState finalState = result.get();
				String report = finalState.value("result").get().toString();

				// 更新任务状态
				task.setStatus(MathTask.TaskStatus.COMPLETED);
				task.setCompletedAt(LocalDateTime.now());
				task.setExecutionTimeMs(calculateExecutionTime(task));
				mathTaskService.updateTask(task);

				// 保存最终结果
				TaskResult finalResult = TaskResult.builder()
					.taskId(task.getId())
					.nodeName("FINAL_REPORT")
					.status(TaskResult.NodeStatus.SUCCESS)
					.outputData(report)
					.build();
				mathTaskService.saveTaskResult(task.getId(), "FINAL_REPORT", TaskResult.NodeStatus.SUCCESS, null,
						report, null);

				mathTaskService.addTaskLog(task.getId(), "GRAPH_EXECUTOR", TaskLog.LogLevel.INFO, "Graph工作流执行完成",
						report);

				return "execution_" + System.currentTimeMillis();
			}
			else {
				throw new RuntimeException("Graph工作流执行失败");
			}

		}
		catch (Exception e) {
			log.error("Graph工作流执行失败", e);

			// 更新任务状态为失败
			task.setStatus(MathTask.TaskStatus.FAILED);
			task.setCompletedAt(LocalDateTime.now());
			mathTaskService.updateTask(task);

			mathTaskService.addTaskLog(task.getId(), "GRAPH_EXECUTOR", TaskLog.LogLevel.ERROR,
					"Graph工作流执行失败: " + e.getMessage(), null);

			throw e;
		}
	}

	private Long calculateExecutionTime(MathTask task) {
		if (task.getStartedAt() != null && task.getCompletedAt() != null) {
			return java.time.Duration.between(task.getStartedAt(), task.getCompletedAt()).toMillis();
		}
		return null;
	}

	/**
	 * 创建任务请求DTO
	 */
	public static class CreateTaskRequest {

		private String title;

		private String description;

		private String problemStatement;

		private String researchGoals;

		private String dataRequirements;

		private String modelConstraints;

		private String expectedOutputs;

		private MathTask.TaskType type;

		// Getters and Setters
		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getProblemStatement() {
			return problemStatement;
		}

		public void setProblemStatement(String problemStatement) {
			this.problemStatement = problemStatement;
		}

		public String getResearchGoals() {
			return researchGoals;
		}

		public void setResearchGoals(String researchGoals) {
			this.researchGoals = researchGoals;
		}

		public String getDataRequirements() {
			return dataRequirements;
		}

		public void setDataRequirements(String dataRequirements) {
			this.dataRequirements = dataRequirements;
		}

		public String getModelConstraints() {
			return modelConstraints;
		}

		public void setModelConstraints(String modelConstraints) {
			this.modelConstraints = modelConstraints;
		}

		public String getExpectedOutputs() {
			return expectedOutputs;
		}

		public void setExpectedOutputs(String expectedOutputs) {
			this.expectedOutputs = expectedOutputs;
		}

		public MathTask.TaskType getType() {
			return type;
		}

		public void setType(MathTask.TaskType type) {
			this.type = type;
		}

	}

}
