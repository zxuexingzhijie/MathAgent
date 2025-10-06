package com.mathmodel.core.interpreter;

import com.mathmodel.config.MathModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Local Jupyter/Python Interpreter
 * Executes Python code using local Python installation
 */
@Slf4j
@Component
public class LocalJupyterInterpreter implements CodeInterpreter {

    private static final int TIMEOUT_SECONDS = 300; // 5 minutes
    
    private final MathModelProperties properties;
    private boolean initialized = false;

    public LocalJupyterInterpreter(MathModelProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize() throws Exception {
        log.info("[LocalJupyterInterpreter] Initializing Python environment");
        
        // Check if Python is available
        ProcessBuilder pb = new ProcessBuilder("python", "--version");
        pb.redirectErrorStream(true);
        
        try {
            Process process = pb.start();
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroy();
                throw new RuntimeException("Python version check timed out");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("Python not found. Please install Python 3.8+");
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
            String version = reader.readLine();
            log.info("[LocalJupyterInterpreter] Python version: {}", version);
            
            initialized = true;
        } catch (IOException | InterruptedException e) {
            log.error("[LocalJupyterInterpreter] Failed to initialize", e);
            throw new RuntimeException("Failed to initialize Python interpreter", e);
        }
    }

    @Override
    public boolean isReady() {
        return initialized;
    }

    @Override
    public ExecutionResult execute(String code, String taskId) throws Exception {
        if (!initialized) {
            initialize();
        }

        long startTime = System.currentTimeMillis();
        log.info("[LocalJupyterInterpreter] Executing code for task: {}", taskId);

        // Create temporary Python file
        Path workDir = Paths.get(properties.getWorkDir(), taskId);
        Path codeDir = workDir.resolve("code");
        Path imageDir = workDir.resolve("images");
        
        Files.createDirectories(codeDir);
        Files.createDirectories(imageDir);
        
        Path scriptFile = codeDir.resolve("script_" + System.currentTimeMillis() + ".py");
        
        // Enhance code with image directory setup
        String enhancedCode = enhanceCodeWithImagePath(code, imageDir.toString());
        Files.writeString(scriptFile, enhancedCode, StandardCharsets.UTF_8);
        
        log.debug("[LocalJupyterInterpreter] Script file: {}", scriptFile);

        // Execute Python script
        ProcessBuilder pb = new ProcessBuilder("python", scriptFile.toString());
        pb.directory(codeDir.toFile());
        pb.redirectErrorStream(false);

        try {
            Process process = pb.start();
            
            // Read stdout
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("[LocalJupyterInterpreter] Error reading stdout", e);
                }
            });
            
            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        error.append(line).append("\n");
                    }
                } catch (IOException e) {
                    log.error("[LocalJupyterInterpreter] Error reading stderr", e);
                }
            });
            
            outputThread.start();
            errorThread.start();
            
            // Wait for completion with timeout
            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            
            outputThread.join(5000);
            errorThread.join(5000);
            
            if (!completed) {
                process.destroy();
                long elapsed = System.currentTimeMillis() - startTime;
                log.error("[LocalJupyterInterpreter] Execution timeout after {} seconds", TIMEOUT_SECONDS);
                return ExecutionResult.failure(
                    "Code execution timed out after " + TIMEOUT_SECONDS + " seconds", 
                    elapsed
                );
            }
            
            int exitCode = process.exitValue();
            long elapsed = System.currentTimeMillis() - startTime;
            
            // Find created images
            List<String> createdImages = findCreatedImages(imageDir, code);
            
            if (exitCode != 0) {
                log.error("[LocalJupyterInterpreter] Execution failed with exit code: {}", exitCode);
                log.error("[LocalJupyterInterpreter] Error output: {}", error);
                return ExecutionResult.failure(error.toString(), elapsed);
            }
            
            log.info("[LocalJupyterInterpreter] Execution successful in {}ms", elapsed);
            return ExecutionResult.success(output.toString(), createdImages, elapsed);
            
        } catch (IOException | InterruptedException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.error("[LocalJupyterInterpreter] Execution error", e);
            return ExecutionResult.failure(e.getMessage(), elapsed);
        } finally {
            // Optionally delete script file (keep for debugging)
            // Files.deleteIfExists(scriptFile);
        }
    }

    @Override
    public void shutdown() {
        log.info("[LocalJupyterInterpreter] Shutdown");
        initialized = false;
    }

    /**
     * Enhance code to use correct image directory
     */
    private String enhanceCodeWithImagePath(String code, String imageDir) {
        // Add matplotlib configuration at the beginning
        String setup = """
            import os
            import sys
            import matplotlib
            matplotlib.use('Agg')  # Use non-interactive backend
            import matplotlib.pyplot as plt
            
            # Set image output directory
            IMAGE_DIR = r'%s'
            os.makedirs(IMAGE_DIR, exist_ok=True)
            
            # Helper function to save figures
            def save_figure(filename, dpi=300):
                filepath = os.path.join(IMAGE_DIR, filename)
                plt.savefig(filepath, dpi=dpi, bbox_inches='tight')
                print(f'Saved figure: {filepath}')
                return filepath
            
            """.formatted(imageDir);
        
        return setup + "\n" + code;
    }

    /**
     * Find images created during code execution
     */
    private List<String> findCreatedImages(Path imageDir, String code) {
        List<String> images = new ArrayList<>();
        
        try {
            if (!Files.exists(imageDir)) {
                return images;
            }
            
            // List all image files in the directory
            try (var stream = Files.list(imageDir)) {
                stream.filter(Files::isRegularFile)
                      .filter(p -> {
                          String name = p.getFileName().toString().toLowerCase();
                          return name.endsWith(".png") || 
                                 name.endsWith(".jpg") || 
                                 name.endsWith(".jpeg") ||
                                 name.endsWith(".svg") ||
                                 name.endsWith(".pdf");
                      })
                      .forEach(p -> images.add(p.toString()));
            }
            
            log.info("[LocalJupyterInterpreter] Found {} images", images.size());
            
        } catch (IOException e) {
            log.error("[LocalJupyterInterpreter] Error finding images", e);
        }
        
        return images;
    }
}
