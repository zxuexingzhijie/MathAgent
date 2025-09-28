package com.mathagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Code Interpreter服务 基于Jupyter的代码执行环境
 */
@Slf4j
@Service
public class CodeInterpreterService {

	@Autowired
	private ObjectMapper objectMapper;

	private final Map<String, JupyterSession> sessions = new HashMap<>();

	private final String notebookBasePath = "notebooks";

	/**
	 * 创建新的Jupyter会话
	 */
	public JupyterSession createSession(String taskId) {
		try {
			String sessionId = "session_" + taskId + "_" + System.currentTimeMillis();
			JupyterSession session = new JupyterSession(sessionId, taskId);
			sessions.put(sessionId, session);

			// 创建notebook文件
			createNotebookFile(session);

			log.info("创建Jupyter会话: {}", sessionId);
			return session;
		}
		catch (Exception e) {
			log.error("创建Jupyter会话失败", e);
			throw new RuntimeException("创建Jupyter会话失败", e);
		}
	}

	/**
	 * 执行Python代码
	 */
	public CodeExecutionResult executeCode(String sessionId, String code, String language) {
		JupyterSession session = sessions.get(sessionId);
		if (session == null) {
			throw new RuntimeException("会话不存在: " + sessionId);
		}

		try {
			// 添加代码到notebook
			CodeCell cell = new CodeCell(code, language);
			session.addCell(cell);

			// 执行代码
			CodeExecutionResult result = executeCodeCell(cell);
			cell.setExecutionResult(result);

			// 保存notebook
			saveNotebook(session);

			log.info("代码执行完成: {}", sessionId);
			return result;
		}
		catch (Exception e) {
			log.error("代码执行失败", e);
			CodeExecutionResult errorResult = new CodeExecutionResult();
			errorResult.setSuccess(false);
			errorResult.setError(e.getMessage());
			return errorResult;
		}
	}

	/**
	 * 获取会话的notebook内容
	 */
	public String getNotebookContent(String sessionId) {
		JupyterSession session = sessions.get(sessionId);
		if (session == null) {
			throw new RuntimeException("会话不存在: " + sessionId);
		}

		try {
			Path notebookPath = Paths.get(notebookBasePath, session.getTaskId(), "notebook.ipynb");
			return Files.readString(notebookPath);
		}
		catch (Exception e) {
			log.error("读取notebook失败", e);
			throw new RuntimeException("读取notebook失败", e);
		}
	}

	/**
	 * 获取会话的所有代码单元格
	 */
	public List<CodeCell> getSessionCells(String sessionId) {
		JupyterSession session = sessions.get(sessionId);
		if (session == null) {
			throw new RuntimeException("会话不存在: " + sessionId);
		}
		return new ArrayList<>(session.getCells());
	}

	/**
	 * 清理会话
	 */
	public void cleanupSession(String sessionId) {
		JupyterSession session = sessions.remove(sessionId);
		if (session != null) {
			log.info("清理Jupyter会话: {}", sessionId);
		}
	}

	/**
	 * 执行代码单元格
	 */
	private CodeExecutionResult executeCodeCell(CodeCell cell) {
		CodeExecutionResult result = new CodeExecutionResult();
		result.setStartTime(LocalDateTime.now());

		try {
			// 这里应该集成真实的Python执行环境
			// 为了演示，我们模拟执行结果
			String output = simulateCodeExecution(cell.getCode());

			result.setSuccess(true);
			result.setOutput(output);
			result.setEndTime(LocalDateTime.now());

		}
		catch (Exception e) {
			result.setSuccess(false);
			result.setError(e.getMessage());
			result.setEndTime(LocalDateTime.now());
		}

		return result;
	}

	/**
	 * 模拟代码执行（实际应该调用Python解释器）
	 */
	private String simulateCodeExecution(String code) {
		// 这里应该调用真实的Python解释器
		// 例如使用ProcessBuilder或Jython
		return "模拟执行结果: " + code;
	}

	/**
	 * 创建notebook文件
	 */
	private void createNotebookFile(JupyterSession session) throws IOException {
		Path taskDir = Paths.get(notebookBasePath, session.getTaskId());
		Files.createDirectories(taskDir);

		Path notebookPath = taskDir.resolve("notebook.ipynb");

		Map<String, Object> notebook = new HashMap<>();
		notebook.put("cells", new ArrayList<>());
		notebook.put("metadata", new HashMap<>());
		notebook.put("nbformat", 4);
		notebook.put("nbformat_minor", 4);

		Files.writeString(notebookPath, objectMapper.writeValueAsString(notebook));
	}

	/**
	 * 保存notebook
	 */
	private void saveNotebook(JupyterSession session) throws IOException {
		Path notebookPath = Paths.get(notebookBasePath, session.getTaskId(), "notebook.ipynb");

		Map<String, Object> notebook = new HashMap<>();
		notebook.put("cells", session.getCells());
		notebook.put("metadata", new HashMap<>());
		notebook.put("nbformat", 4);
		notebook.put("nbformat_minor", 4);

		Files.writeString(notebookPath, objectMapper.writeValueAsString(notebook));
	}

	/**
	 * Jupyter会话
	 */
	public static class JupyterSession {

		private String sessionId;

		private String taskId;

		private List<CodeCell> cells;

		private LocalDateTime createdAt;

		public JupyterSession(String sessionId, String taskId) {
			this.sessionId = sessionId;
			this.taskId = taskId;
			this.cells = new ArrayList<>();
			this.createdAt = LocalDateTime.now();
		}

		public void addCell(CodeCell cell) {
			this.cells.add(cell);
		}

		// Getters and Setters
		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}

		public List<CodeCell> getCells() {
			return cells;
		}

		public void setCells(List<CodeCell> cells) {
			this.cells = cells;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

	}

	/**
	 * 代码单元格
	 */
	public static class CodeCell {

		private String code;

		private String language;

		private CodeExecutionResult executionResult;

		private LocalDateTime createdAt;

		public CodeCell(String code, String language) {
			this.code = code;
			this.language = language;
			this.createdAt = LocalDateTime.now();
		}

		// Getters and Setters
		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public CodeExecutionResult getExecutionResult() {
			return executionResult;
		}

		public void setExecutionResult(CodeExecutionResult executionResult) {
			this.executionResult = executionResult;
		}

		public LocalDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(LocalDateTime createdAt) {
			this.createdAt = createdAt;
		}

	}

	/**
	 * 代码执行结果
	 */
	public static class CodeExecutionResult {

		private boolean success;

		private String output;

		private String error;

		private LocalDateTime startTime;

		private LocalDateTime endTime;

		// Getters and Setters
		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}

		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public LocalDateTime getStartTime() {
			return startTime;
		}

		public void setStartTime(LocalDateTime startTime) {
			this.startTime = startTime;
		}

		public LocalDateTime getEndTime() {
			return endTime;
		}

		public void setEndTime(LocalDateTime endTime) {
			this.endTime = endTime;
		}

	}

}
