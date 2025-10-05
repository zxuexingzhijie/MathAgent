package com.mathmodel.core.prompts;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for PromptLoader
 */
@SpringBootTest
class PromptLoaderTest {

    @Autowired
    private PromptLoader promptLoader;

    @Test
    void testPromptLoaderInitialization() {
        assertNotNull(promptLoader, "PromptLoader should be initialized");
    }

    @Test
    void testGetCoordinatorPrompt() {
        String prompt = promptLoader.getCoordinatorPrompt();
        
        assertNotNull(prompt, "Coordinator prompt should not be null");
        assertFalse(prompt.isEmpty(), "Coordinator prompt should not be empty");
        assertTrue(prompt.contains("数学建模"), "Coordinator prompt should contain '数学建模'");
        assertTrue(prompt.contains("JSON"), "Coordinator prompt should contain 'JSON'");
    }

    @Test
    void testGetModelerPrompt() {
        String prompt = promptLoader.getModelerPrompt();
        
        assertNotNull(prompt, "Modeler prompt should not be null");
        assertFalse(prompt.isEmpty(), "Modeler prompt should not be empty");
        assertTrue(prompt.contains("数学模型"), "Modeler prompt should contain '数学模型'");
    }

    @Test
    void testGetCoderPrompt() {
        String prompt = promptLoader.getCoderPrompt();
        
        assertNotNull(prompt, "Coder prompt should not be null");
        assertFalse(prompt.isEmpty(), "Coder prompt should not be empty");
        assertTrue(prompt.contains("Python"), "Coder prompt should contain 'Python'");
    }

    @Test
    void testGetWriterPrompt() {
        String prompt = promptLoader.getWriterPrompt();
        
        assertNotNull(prompt, "Writer prompt should not be null");
        assertFalse(prompt.isEmpty(), "Writer prompt should not be empty");
        assertTrue(prompt.contains("论文"), "Writer prompt should contain '论文'");
    }

    @Test
    void testGetReflectionPrompt() {
        String errorMessage = "IndexError: list index out of range";
        int retryCount = 2;
        
        String prompt = promptLoader.getReflectionPrompt(errorMessage, retryCount);
        
        assertNotNull(prompt, "Reflection prompt should not be null");
        assertFalse(prompt.isEmpty(), "Reflection prompt should not be empty");
        assertTrue(prompt.contains(errorMessage), "Reflection prompt should contain error message");
        assertTrue(prompt.contains(String.valueOf(retryCount)), "Reflection prompt should contain retry count");
    }

    @Test
    void testIsPromptLoaded() {
        assertTrue(promptLoader.isPromptLoaded("coordinator.txt"), 
                "coordinator.txt should be loaded");
        assertTrue(promptLoader.isPromptLoaded("modeler.txt"), 
                "modeler.txt should be loaded");
        assertTrue(promptLoader.isPromptLoaded("coder.txt"), 
                "coder.txt should be loaded");
        assertTrue(promptLoader.isPromptLoaded("writer.txt"), 
                "writer.txt should be loaded");
        assertTrue(promptLoader.isPromptLoaded("reflection.txt"), 
                "reflection.txt should be loaded");
        
        assertFalse(promptLoader.isPromptLoaded("nonexistent.txt"), 
                "nonexistent.txt should not be loaded");
    }

    @Test
    void testGetAllPrompts() {
        var allPrompts = promptLoader.getAllPrompts();
        
        assertNotNull(allPrompts, "All prompts map should not be null");
        assertEquals(5, allPrompts.size(), "Should have 5 prompts loaded");
        assertTrue(allPrompts.containsKey("coordinator.txt"));
        assertTrue(allPrompts.containsKey("modeler.txt"));
        assertTrue(allPrompts.containsKey("coder.txt"));
        assertTrue(allPrompts.containsKey("writer.txt"));
        assertTrue(allPrompts.containsKey("reflection.txt"));
    }

    @Test
    void testReloadPrompt() {
        // Get initial prompt
        String initialPrompt = promptLoader.getCoordinatorPrompt();
        
        // Reload prompt
        promptLoader.reloadPrompt("coordinator.txt");
        
        // Get prompt again
        String reloadedPrompt = promptLoader.getCoordinatorPrompt();
        
        // Should be the same content
        assertEquals(initialPrompt, reloadedPrompt, 
                "Reloaded prompt should have same content");
    }

    @Test
    void testReloadAllPrompts() {
        // Store initial prompts
        var initialPrompts = promptLoader.getAllPrompts();
        
        // Reload all prompts
        promptLoader.reloadAllPrompts();
        
        // Get prompts again
        var reloadedPrompts = promptLoader.getAllPrompts();
        
        // Should have same number of prompts
        assertEquals(initialPrompts.size(), reloadedPrompts.size(), 
                "Should have same number of prompts after reload");
    }

    @Test
    void testGetNonExistentPrompt() {
        assertThrows(IllegalArgumentException.class, 
                () -> promptLoader.getPrompt("nonexistent.txt"),
                "Should throw exception for non-existent prompt");
    }
}
