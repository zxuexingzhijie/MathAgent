package com.mathagent.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 任务日志实体 记录任务执行过程中的日志信息
 * 
 * @author Makoto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskLog {

	private Long id;

	private Long taskId;

	private String nodeName;

	private LogLevel level;

	private String message;

	private String details;

	private LocalDateTime timestamp;

	public enum LogLevel {

		DEBUG("调试"), INFO("信息"), WARN("警告"), ERROR("错误"), FATAL("致命");

		private final String description;

		LogLevel(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

	}

}