package com.mathagent.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数学建模任务实体 代表一个完整的数学建模研究任务
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MathTask {

	private Long id;

	private String title;

	private String description;

	private TaskStatus status;

	private TaskType type;

	private String problemStatement;

	private String researchGoals;

	private String dataRequirements;

	private String modelConstraints;

	private String expectedOutputs;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime startedAt;

	private LocalDateTime completedAt;

	private Long executionTimeMs;

	private String graphExecutionId;

	public enum TaskStatus {

		CREATED("已创建"), ANALYZING("分析中"), DATA_COLLECTING("数据收集中"), MODEL_BUILDING("模型构建中"), SOLVING("求解中"),
		GENERATING_REPORT("生成报告中"), COMPLETED("已完成"), FAILED("失败"), CANCELLED("已取消");

		private final String description;

		TaskStatus(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	}

	public enum TaskType {

		OPTIMIZATION("优化问题"), PREDICTION("预测问题"), CLASSIFICATION("分类问题"), SIMULATION("仿真问题"),
		STATISTICAL_ANALYSIS("统计分析"), MACHINE_LEARNING("机器学习"), COMPLEX_SYSTEM("复杂系统"), OTHER("其他");

		private final String description;

		TaskType(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	}

}
