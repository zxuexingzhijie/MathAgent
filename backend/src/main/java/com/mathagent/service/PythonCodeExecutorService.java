package com.mathagent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathagent.exception.PythonExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Python代码执行服务 - 基于ProcessBuilder的Python解释器
 * 
 * @author Makoto
 */
@Slf4j
@Service
public class PythonCodeExecutorService {

	@Autowired
	private ObjectMapper objectMapper;

	private final Map<String, PythonSession> sessions = new ConcurrentHashMap<>();
	private final String codeBasePath = "python_code";

	/**
	 * 创建新的Python执行会话
	 */
	public PythonSession createSession(String taskId) throws PythonExecutionException {
		try {
			String sessionId = "session_" + taskId + "_" + System.currentTimeMillis();
			PythonSession session = new PythonSession(sessionId, taskId);
			sessions.put(sessionId, session);

			// 创建代码目录
			createCodeDirectory(session);

			log.info("创建Python执行会话: {}", sessionId);
			return session;
		}
		catch (IOException e) {
			throw new PythonExecutionException("", "", "创建Python执行会话失败", e);
		}
	}

	/**
	 * 执行Python代码
	 */
	public CodeExecutionResult executeCode(String sessionId, String code, String language) throws PythonExecutionException {
		PythonSession session = sessions.get(sessionId);
		if (session == null) {
			throw new PythonExecutionException(sessionId, code, "会话不存在: " + sessionId);
		}

		try {
			// 添加代码到会话
			CodeCell cell = new CodeCell(code, language);
			session.addCell(cell);

			// 执行代码
			CodeExecutionResult result = executeCodeCell(cell);
			cell.setExecutionResult(result);

			// 保存代码历史
			saveCodeHistory(session);

			log.info("代码执行完成: {}", sessionId);
			return result;
		}
		catch (IOException e) {
			throw new PythonExecutionException(sessionId, code, "代码执行失败", e);
		}
	}

	/**
	 * 获取会话的代码历史内容
	 */
	public String getCodeHistory(String sessionId) throws PythonExecutionException {
		PythonSession session = sessions.get(sessionId);
		if (session == null) {
			throw new PythonExecutionException(sessionId, "", "会话不存在: " + sessionId);
		}

		try {
			Path codePath = Paths.get(codeBasePath, session.getTaskId(), "code_history.json");
			return Files.readString(codePath);
		}
		catch (IOException e) {
			throw new PythonExecutionException(sessionId, "", "读取代码历史失败", e);
		}
	}

	/**
	 * 获取会话的所有代码单元格
	 */
	public List<CodeCell> getSessionCells(String sessionId) throws PythonExecutionException {
		PythonSession session = sessions.get(sessionId);
		if (session == null) {
			throw new PythonExecutionException(sessionId, "", "会话不存在: " + sessionId);
		}
		return new ArrayList<>(session.getCells());
	}

	/**
	 * 清理会话
	 */
	public void cleanupSession(String sessionId) {
		PythonSession session = sessions.remove(sessionId);
		if (session != null) {
			log.info("清理Python执行会话: {}", sessionId);
		}
	}


	/**
	 * 执行代码单元格
	 */
	private CodeExecutionResult executeCodeCell(CodeCell cell) {
		CodeExecutionResult result = new CodeExecutionResult();
		result.setStartTime(LocalDateTime.now());

		try {
			// 创建临时Python文件
			Path tempFile = createTempPythonFile(cell.getCode());
			
			// 构建Python执行命令
			ProcessBuilder processBuilder = new ProcessBuilder(
				"python", tempFile.toString()
			);
			
		// 设置工作目录和环境变量
		processBuilder.directory(Paths.get(codeBasePath).toFile());
		processBuilder.environment().put("PYTHONPATH", codeBasePath);
			
			// 执行Python代码
			Process process = processBuilder.start();
			
			// 读取输出和错误
			String output = readProcessOutput(process.getInputStream());
			String error = readProcessError(process.getErrorStream());
			
			// 等待进程完成
			int exitCode = process.waitFor();
			
			result.setSuccess(exitCode == 0);
			result.setOutput(output);
			result.setError(error);
			result.setEndTime(LocalDateTime.now());
			
			// 清理临时文件
			Files.deleteIfExists(tempFile);
			
		}
		catch (Exception e) {
			result.setSuccess(false);
			result.setError(e.getMessage());
			result.setEndTime(LocalDateTime.now());
		}

		return result;
	}

	/**
	 * 创建临时Python文件
	 */
	private Path createTempPythonFile(String code) throws IOException {
		// 检查代码安全性
		if (!isCodeSafe(code)) {
			throw new SecurityException("代码包含不安全的操作，执行被拒绝");
		}
		
		String sessionId = UUID.randomUUID().toString();
		Path tempFile = Paths.get(codeBasePath, sessionId + ".py");
		
		// 添加必要的import和设置
		StringBuilder fullCode = new StringBuilder();
		fullCode.append("import sys\n");
		fullCode.append("import numpy as np\n");
		fullCode.append("import matplotlib.pyplot as plt\n");
		fullCode.append("import pandas as pd\n");
		fullCode.append("import scipy\n");
		fullCode.append("import json\n");
		fullCode.append("\n");
		fullCode.append(code);
		
		Files.writeString(tempFile, fullCode.toString());
		return tempFile;
	}

	/**
	 * 检查代码安全性
	 */
	private boolean isCodeSafe(String code) {
		// 检查危险操作
		String[] dangerousPatterns = {
			"import os", "subprocess", "eval(", "exec(",
			"__import__", "open(", "file(", "input(",
			"system(", "popen(", "spawn", "fork",
			"rm ", "del ", "format(", "compile("
		};
		
		String lowerCode = code.toLowerCase();
		for (String pattern : dangerousPatterns) {
			if (lowerCode.contains(pattern)) {
				log.warn("检测到危险操作: {}", pattern);
				return false;
			}
		}
		return true;
	}

	/**
	 * 读取进程输出
	 */
	private String readProcessOutput(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

	/**
	 * 读取进程错误
	 */
	private String readProcessError(InputStream errorStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
			return reader.lines().collect(Collectors.joining("\n"));
		}
	}

	/**
	 * 创建代码目录
	 */
	private void createCodeDirectory(PythonSession session) throws IOException {
		Path taskDir = Paths.get(codeBasePath, session.getTaskId());
		Files.createDirectories(taskDir);

		Path codePath = taskDir.resolve("code_history.json");

		Map<String, Object> codeHistory = new HashMap<>();
		codeHistory.put("cells", new ArrayList<>());
		codeHistory.put("metadata", new HashMap<>());
		codeHistory.put("created_at", LocalDateTime.now().toString());

		Files.writeString(codePath, objectMapper.writeValueAsString(codeHistory));
	}

	/**
	 * 保存代码历史
	 */
	private void saveCodeHistory(PythonSession session) throws IOException {
		Path codePath = Paths.get(codeBasePath, session.getTaskId(), "code_history.json");

		Map<String, Object> codeHistory = new HashMap<>();
		codeHistory.put("cells", session.getCells());
		codeHistory.put("metadata", new HashMap<>());
		codeHistory.put("updated_at", LocalDateTime.now().toString());

		Files.writeString(codePath, objectMapper.writeValueAsString(codeHistory));
	}

	/**
	 * Python执行会话
	 */
	public static class PythonSession {

		private String sessionId;

		private String taskId;

		private List<CodeCell> cells;

		private LocalDateTime createdAt;

		public PythonSession(String sessionId, String taskId) {
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
