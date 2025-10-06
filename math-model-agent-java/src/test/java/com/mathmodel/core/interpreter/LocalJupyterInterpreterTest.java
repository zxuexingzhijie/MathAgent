package com.mathmodel.core.interpreter;

import com.mathmodel.config.MathModelProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for LocalJupyterInterpreter
 */
class LocalJupyterInterpreterTest {

    private LocalJupyterInterpreter interpreter;
    private MathModelProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MathModelProperties();
        properties.setWorkDir("target/test-work-dir");
        interpreter = new LocalJupyterInterpreter(properties);
    }

    /**
     * Check if Python is available in the system
     */
    static boolean isPythonAvailable() {
        try {
            Process process = new ProcessBuilder("python", "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testInitialize() throws Exception {
        interpreter.initialize();
        assertTrue(interpreter.isReady());
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testSimpleCodeExecution() throws Exception {
        interpreter.initialize();

        String code = """
            print("Hello from Python!")
            result = 2 + 2
            print(f"Result: {result}")
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-001");

        assertTrue(result.success());
        assertNotNull(result.output());
        assertTrue(result.output().contains("Hello from Python!"));
        assertTrue(result.output().contains("Result: 4"));
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testMathExecution() throws Exception {
        interpreter.initialize();

        String code = """
            import numpy as np
            
            # Calculate mean
            data = np.array([1, 2, 3, 4, 5])
            mean = np.mean(data)
            print(f"Mean: {mean}")
            
            # Calculate sum
            total = np.sum(data)
            print(f"Sum: {total}")
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-002");

        assertTrue(result.success());
        assertTrue(result.output().contains("Mean: 3.0"));
        assertTrue(result.output().contains("Sum: 15"));
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testImageGeneration() throws Exception {
        interpreter.initialize();

        String code = """
            import matplotlib.pyplot as plt
            import numpy as np
            
            # Generate simple plot
            x = np.linspace(0, 10, 100)
            y = np.sin(x)
            
            plt.figure(figsize=(8, 6))
            plt.plot(x, y)
            plt.title('Sine Wave')
            plt.xlabel('X')
            plt.ylabel('Y')
            
            # Use the helper function to save
            save_figure('test_sine_wave.png')
            plt.close()
            
            print("Plot generated successfully")
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-003");

        assertTrue(result.success());
        assertFalse(result.createdImages().isEmpty());
        assertTrue(result.createdImages().get(0).contains("test_sine_wave.png"));
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testExecutionError() throws Exception {
        interpreter.initialize();

        String code = """
            # This will cause a ZeroDivisionError
            result = 1 / 0
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-004");

        assertFalse(result.success());
        assertNotNull(result.error());
        assertTrue(result.error().contains("ZeroDivisionError"));
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testSyntaxError() throws Exception {
        interpreter.initialize();

        String code = """
            # Syntax error: missing colon
            if True
                print("test")
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-005");

        assertFalse(result.success());
        assertNotNull(result.error());
        assertTrue(result.error().toLowerCase().contains("syntax"));
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testMultipleImages() throws Exception {
        interpreter.initialize();

        String code = """
            import matplotlib.pyplot as plt
            import numpy as np
            
            # Generate multiple plots
            for i in range(3):
                plt.figure(figsize=(6, 4))
                x = np.linspace(0, 10, 100)
                y = np.sin(x * (i + 1))
                plt.plot(x, y)
                plt.title(f'Sine Wave {i+1}')
                save_figure(f'plot_{i+1}.png')
                plt.close()
            
            print(f"Generated 3 plots")
            """;

        CodeInterpreter.ExecutionResult result = interpreter.execute(code, "test-task-006");

        assertTrue(result.success());
        assertEquals(3, result.createdImages().size());
    }

    @Test
    void testNotInitialized() {
        // Should auto-initialize
        assertDoesNotThrow(() -> {
            String code = "print('test')";
            interpreter.execute(code, "test-task-007");
        });
    }

    @Test
    @EnabledIf("isPythonAvailable")
    void testShutdown() throws Exception {
        interpreter.initialize();
        assertTrue(interpreter.isReady());
        
        interpreter.shutdown();
        assertFalse(interpreter.isReady());
    }
}
